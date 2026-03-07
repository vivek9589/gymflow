package com.gymflow.gymflow.auth.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String email;
    private Long gymId;
    private String role;
}