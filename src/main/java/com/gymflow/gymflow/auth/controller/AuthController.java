package com.gymflow.gymflow.auth.controller;

import com.gymflow.gymflow.auth.dto.request.LoginRequest;
import com.gymflow.gymflow.auth.dto.request.OwnerRegisterRequest;
import com.gymflow.gymflow.auth.dto.response.LoginResponse;
import com.gymflow.gymflow.auth.service.AuthService;
import com.gymflow.gymflow.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        // Service handles the logic and throws BusinessException if anything fails
        LoginResponse response = authService.login(request);

        // Return structured response for React frontend
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful! Welcome back."));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody OwnerRegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Registration successful! You can now login."));
    }
}