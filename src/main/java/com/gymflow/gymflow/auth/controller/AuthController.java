package com.gymflow.gymflow.auth.controller;

import com.gymflow.gymflow.auth.dto.request.*;
import com.gymflow.gymflow.auth.dto.response.LoginResponse;
import com.gymflow.gymflow.auth.entity.GymOwner;
import com.gymflow.gymflow.auth.service.AuthService;
import com.gymflow.gymflow.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication-related APIs.
 * Provides endpoints for login, registration, logout, profile management, and password operations.
 */


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email={}", request.getEmail());
        LoginResponse response = authService.login(request);
        log.info("Login successful for email={}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful! Welcome back."));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody OwnerRegisterRequest request) {
        log.info("Registration attempt for owner email={}", request.getEmail());
        authService.register(request);
        log.info("Registration successful for owner email={}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null, "Registration successful! You can now login."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        authService.logout(token);
        log.info("Logout successful for token={}", token);
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        log.info("Password reset link sent to email={}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset link sent to your email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        log.info("Password reset successful for token={}", request.getToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successful."));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<GymOwner>> getProfile(Authentication authentication) {
        String email = authentication.getName();
        GymOwner profile = authService.getProfile(email);
        log.info("Profile fetched successfully for email={}", email);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile fetched successfully."));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<String>> updateProfile(Authentication authentication,
                                                             @Valid @RequestBody UpdateProfileRequest request) {
        String email = authentication.getName();
        authService.updateProfile(email, request);
        log.info("Profile updated successfully for email={}", email);
        return ResponseEntity.ok(ApiResponse.success(null, "Profile updated successfully."));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(Authentication authentication,
                                                              @Valid @RequestBody ChangePasswordRequest request) {
        String email = authentication.getName();
        authService.changePassword(email, request.getOldPassword(), request.getNewPassword());
        log.info("Password changed successfully for email={}", email);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully."));
    }
}