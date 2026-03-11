package com.gymflow.gymflow.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response DTO returned after successful login.
 * Contains JWT token and basic user details.
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String email;
    private Long gymId;
    private String role;
}