package com.gymflow.gymflow.auth.service.impl;

import com.gymflow.gymflow.auth.dto.request.LoginRequest;
import com.gymflow.gymflow.auth.dto.request.OwnerRegisterRequest;
import com.gymflow.gymflow.auth.dto.request.UpdateProfileRequest;
import com.gymflow.gymflow.auth.dto.response.LoginResponse;
import com.gymflow.gymflow.auth.entity.GymOwner;
import com.gymflow.gymflow.auth.enums.Role;
import com.gymflow.gymflow.auth.repository.GymOwnerRepository;
import com.gymflow.gymflow.auth.security.JwtUtil;
import com.gymflow.gymflow.auth.service.AuthService;
import com.gymflow.gymflow.common.exception.InvalidCredentialsException;
import com.gymflow.gymflow.common.exception.UserAlreadyExistsException;
import com.gymflow.gymflow.common.exception.UserNotFoundException;
import com.gymflow.gymflow.gym.entity.Gym;
import com.gymflow.gymflow.gym.repository.GymRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.warn("Invalid login attempt for email={}", request.getEmail());
            throw new InvalidCredentialsException();
        }

        GymOwner owner = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found for email={}", request.getEmail());
                    return new UserNotFoundException(request.getEmail());
                });

        String token = jwtUtil.generateToken(
                owner.getEmail(),
                owner.getGym().getId(),
                owner.getGym().getName(),
                owner.getOwnerName(),
                owner.getRole().name()
        );

        log.info("Login successful for email={}, gymId={}", owner.getEmail(), owner.getGym().getId());

        return new LoginResponse(
                token,
                owner.getEmail(),
                owner.getGym().getId(),
                owner.getRole().name()
        );
    }

    @Override
    @Transactional
    public void register(OwnerRegisterRequest request) {
        log.info("Registration attempt for email={}", request.getEmail());

        if (authRepository.existsByEmail(request.getEmail())) {
            log.warn("Duplicate registration attempt for email={}", request.getEmail());
            throw new UserAlreadyExistsException(request.getEmail());
        }

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
                .gymCode(java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .build();

        Gym savedGym = gymRepository.save(gym);

        GymOwner owner = GymOwner.builder()
                .ownerName(request.getOwnerName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole()))
                .gym(savedGym)
                .build();

        authRepository.save(owner);

        log.info("Registration successful for owner email={}, gymId={}", owner.getEmail(), savedGym.getId());
    }

    @Override
    public void logout(String token) {
        log.info("Logout requested for token={}", token);
        // TODO: Implement token blacklist or refresh token invalidation
    }

    @Override
    public void forgotPassword(String email) {
        log.info("Password reset requested for email={}", email);

        GymOwner owner = authRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        // TODO: Generate reset token and send via email service
        log.info("Password reset token generated for email={}", email);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Password reset attempt with token={}", token);

        // TODO: Validate token, fetch user
        GymOwner owner = authRepository.findByEmail("dummy@example.com") // Replace with token lookup
                .orElseThrow(() -> new UserNotFoundException("dummy@example.com"));

        owner.setPassword(passwordEncoder.encode(newPassword));
        authRepository.save(owner);

        log.info("Password reset successful for email={}", owner.getEmail());
    }

    @Override
    public GymOwner getProfile(String email) {
        log.info("Fetching profile for email={}", email);
        return authRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
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