package com.gymflow.gymflow.auth.service.impl;

import com.gymflow.gymflow.auth.dto.request.LoginRequest;
import com.gymflow.gymflow.auth.dto.request.OwnerRegisterRequest;
import com.gymflow.gymflow.auth.dto.request.UpdateProfileRequest;
import com.gymflow.gymflow.auth.dto.response.LoginResponse;
import com.gymflow.gymflow.auth.dto.response.ProfileResponseDTO;
import com.gymflow.gymflow.auth.entity.GymOwner;
import com.gymflow.gymflow.auth.entity.PasswordResetToken;
import com.gymflow.gymflow.auth.enums.Role;
import com.gymflow.gymflow.auth.repository.GymOwnerRepository;
import com.gymflow.gymflow.auth.repository.PasswordResetTokenRepository;
import com.gymflow.gymflow.auth.security.JwtUtil;
import com.gymflow.gymflow.auth.service.AuthService;
import com.gymflow.gymflow.common.exception.InvalidCredentialsException;
import com.gymflow.gymflow.common.exception.InvalidTokenException;
import com.gymflow.gymflow.common.exception.UserAlreadyExistsException;
import com.gymflow.gymflow.common.exception.UserNotFoundException;
import com.gymflow.gymflow.gym.dto.response.GymResponseDTO;
import com.gymflow.gymflow.gym.entity.Gym;
import com.gymflow.gymflow.gym.repository.GymRepository;
import com.gymflow.gymflow.notification.service.EmailService;
import com.gymflow.gymflow.notification.service.EvolutionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of AuthService.
 * Handles login, registration, and extended authentication flows for Gym Owners.
 */


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final GymOwnerRepository authRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final GymRepository gymRepository;
    private final EvolutionService evolutionService;
    private final PasswordResetTokenRepository tokenRepository;
    private final GymOwnerRepository gymOwnerRepository;
    private final EmailService emailService;


    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        GymOwner owner = authRepository.findByEmailWithGym(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        Gym gym = owner.getGym();

        String token = jwtUtil.generateToken(
                owner.getEmail(),
                gym.getId(),
                gym.getName(),
                owner.getOwnerName(),
                owner.getRole().name()
        );

        return new LoginResponse(
                token,
                owner.getEmail(),
                gym.getId(),
                owner.getRole().name()
        );
    }

    @Override
    @Transactional
    public void register(OwnerRegisterRequest request) {

        log.info("Registration attempt for email={}", request.getEmail());

        if (authRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        // STEP 1: Create Gym
        Gym gym = Gym.builder()
                .name(request.getGymName())
                .address(request.getAddress())
                .contactNumber(request.getContactNumber())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .website(request.getWebsite())
                .establishedYear(request.getEstablishedYear())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .whatsappStatus("PENDING")
                .build();

        Gym savedGym = gymRepository.save(gym);

        // STEP 2: Generate instance name (SAFE)
        String instanceName = generateInstanceName(savedGym);

        // STEP 3: Create WhatsApp Instance (IMPORTANT)
        try {
            evolutionService.createInstance(instanceName);

            savedGym.setWhatsappInstance(instanceName);
            savedGym.setWhatsappStatus("QR_READY");

        } catch (Exception e) {
            log.error("Failed to create WhatsApp instance: {}", e.getMessage());

            savedGym.setWhatsappStatus("FAILED");
        }

        gymRepository.save(savedGym); // save once

        // STEP 4: Create Owner
        GymOwner owner = GymOwner.builder()
                .ownerName(request.getOwnerName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole()))
                .gym(savedGym)
                .build();

        authRepository.save(owner);

        // STEP 5: Send Email (optional)
       //  emailService.sendWelcomeEmail(owner.getEmail(), savedGym.getName());

        log.info("Registration completed: gymId={}, instance={}",
                savedGym.getId(), savedGym.getWhatsappInstance());
    }

    private String generateInstanceName(Gym gym) {

        String slug = gym.getName()
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "-");

        return "gymflow-" + gym.getId() + "-" + slug;
    }

    @Override
    public void logout(String token) {
        log.info("Logout requested for token={}", token);
        // TODO: Implement token blacklist or refresh token invalidation
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        log.info("Password reset requested for email={}", email);

        // Security practice: Don't throw UserNotFoundException here.
        // It prevents external actors from fishing/harvesting valid system accounts.
        Optional<GymOwner> ownerOpt = gymOwnerRepository.findByEmail(email);

        if (ownerOpt.isPresent()) {
            GymOwner owner = ownerOpt.get();

            // Evict any existing legacy tokens for this owner before spinning up a new one
            tokenRepository.deleteByGymOwner(owner);

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, owner, 15);
            tokenRepository.save(resetToken);

            // Push background worker thread to process SMTP mail
            emailService.sendPasswordResetEmail(owner.getEmail(), token);
            log.info("Password reset token generated and email dispatched for owner id={}", owner.getId());
        } else {
            log.warn("Password reset link requested for non-existent email registration: {}", email);
        }
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Password reset attempt executing via payload verification token");

        // 1. Fetch token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("The password reset token is invalid or does not exist."));

        // 2. Validate window constraints
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new InvalidTokenException("The password reset token has expired. Please request a new link.");
        }

        // 3. Mutate entity data status
        GymOwner owner = resetToken.getGymOwner();
        owner.setPassword(passwordEncoder.encode(newPassword));
        gymOwnerRepository.save(owner);

        // 4. Purge token to avoid replay attacks
        tokenRepository.delete(resetToken);

        log.info("Password reset successful for gym owner email={}", owner.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getProfile(String email) {
        GymOwner owner = authRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        return ProfileResponseDTO.builder()
                .id(owner.getId())
                .ownerName(owner.getOwnerName())
                .email(owner.getEmail())
                .role(owner.getRole().name())
                .createdAt(owner.getCreatedAt())
                .updatedAt(owner.getUpdatedAt())
                .gym(GymResponseDTO.builder()
                        .id(owner.getGym().getId())
                        .name(owner.getGym().getName())
                        //.gymCode(owner.getGym().getGymCode())
                        .address(owner.getGym().getAddress())
                        .contactNumber(owner.getGym().getContactNumber())
                        .city(owner.getGym().getCity())
                        .state(owner.getGym().getState())
                        .pincode(owner.getGym().getPincode())
                        .website(owner.getGym().getWebsite())
                        .logoUrl(owner.getGym().getLogoUrl())
                        .description(owner.getGym().getDescription())
                        .establishedYear(owner.getGym().getEstablishedYear())
                        .latitude(owner.getGym().getLatitude())
                        .longitude(owner.getGym().getLongitude())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void updateProfile(String email, UpdateProfileRequest request) {
        GymOwner owner = authRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        // Update Owner field
        owner.setOwnerName(request.getOwnerName());

        // Update nested Gym fields
        Gym gym = owner.getGym();
        gym.setContactNumber(request.getContactNumber());
        gym.setCity(request.getCity());
        gym.setState(request.getState());
        gym.setPincode(request.getPincode());
        gym.setWebsite(request.getWebsite());
        gym.setDescription(request.getDescription());
        gym.setLogoUrl(request.getLogoUrl());

        // authRepository.save(owner) is handled by @Transactional automatically,
        // but keeping it is fine.
        authRepository.save(owner);
    }

    @Override
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        log.info("Password change requested for email={}", email);

        GymOwner owner = authRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (!passwordEncoder.matches(oldPassword, owner.getPassword())) {
            log.warn("Invalid old password for email={}", email);
            throw new InvalidCredentialsException();
        }

        owner.setPassword(passwordEncoder.encode(newPassword));
        authRepository.save(owner);

        log.info("Password changed successfully for email={}", email);
    }
}