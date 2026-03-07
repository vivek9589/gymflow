package com.gymflow.gymflow.auth.service.impl;


import com.gymflow.gymflow.auth.dto.request.LoginRequest;
import com.gymflow.gymflow.auth.dto.request.OwnerRegisterRequest;
import com.gymflow.gymflow.auth.dto.response.LoginResponse;
import com.gymflow.gymflow.auth.entity.GymOwner;
import com.gymflow.gymflow.auth.enums.Role;
import com.gymflow.gymflow.auth.repository.GymOwnerRepository;
import com.gymflow.gymflow.auth.security.JwtUtil;
import com.gymflow.gymflow.auth.service.AuthService;
import com.gymflow.gymflow.common.exception.BusinessException;
import com.gymflow.gymflow.gym.entity.Gym;
import com.gymflow.gymflow.gym.repository.GymRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final GymOwnerRepository authRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final GymRepository gymRepository; // Inject GymRepository

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            // 1. Authenticate using Spring Security's AuthenticationManager
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            // Industry Standard: Don't tell them if it was the email or password that was wrong (Security)
            throw new BusinessException("Invalid email or password.");
        }

        // 2. Fetch the Owner details to generate the token with gymId
        GymOwner owner = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User account not found."));

        // 3. Generate Token using your refactored JwtUtil (includes gymId)
        String token = jwtUtil.generateToken(
                owner.getEmail(),
                owner.getGym().getId(),
                owner.getGym().getName(),
                owner.getOwnerName(),
                "OWNER"
        );

        // 4. Return the response object (Controller will wrap this in ApiResponse)
        return new LoginResponse(
                token,
                owner.getEmail(),
                owner.getGym().getId(),
                "OWNER"
        );
    }
    @Override
    @Transactional
    public void register(OwnerRegisterRequest request) {
        // 1. Duplicate Email Check
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email is already registered.");
        }

        // 2. Map & Save Gym
        Gym gym = new Gym();
        gym.setName(request.getGymName());
        gym.setAddress(request.getAddress());
        gym.setContactNumber(request.getContactNumber());
        gym.setCity(request.getCity());
        gym.setState(request.getState());
        gym.setPincode(request.getPincode());
        gym.setWebsite(request.getWebsite());
        gym.setEstablishedYear(request.getEstablishedYear());
        gym.setDescription(request.getDescription());
        gym.setLogoUrl(request.getLogoUrl());

        // Unique Gym Code for QR
        gym.setGymCode(java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase());

        Gym savedGym = gymRepository.save(gym);

        // 3. Map & Save Owner
        GymOwner owner = new GymOwner();
        owner.setOwnerName(request.getOwnerName()); // Don't forget this!
        owner.setEmail(request.getEmail());
        owner.setPassword(passwordEncoder.encode(request.getPassword()));
        owner.setRole(Role.valueOf(request.getRole())); // OWNER by default
        owner.setGym(savedGym);

        authRepository.save(owner);
    }
}