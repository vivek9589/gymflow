package com.gymflow.gymflow.auth.service;

import com.gymflow.gymflow.auth.dto.request.LoginRequest;
import com.gymflow.gymflow.auth.dto.request.OwnerRegisterRequest;
import com.gymflow.gymflow.auth.dto.request.UpdateProfileRequest;
import com.gymflow.gymflow.auth.dto.response.LoginResponse;
import com.gymflow.gymflow.auth.dto.response.ProfileResponseDTO;
import com.gymflow.gymflow.auth.entity.GymOwner;

/**
 * Service interface for authentication-related operations.
 * Defines contract for login, registration, and other auth flows.
 */
public interface AuthService {

    LoginResponse login(LoginRequest request);

    void register(OwnerRegisterRequest request);

    void logout(String token);

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);

    ProfileResponseDTO getProfile(String email);

    void updateProfile(String email, UpdateProfileRequest request);

    void changePassword(String email, String oldPassword, String newPassword);
}