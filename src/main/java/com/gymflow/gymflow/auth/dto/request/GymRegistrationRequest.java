package com.gymflow.gymflow.auth.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GymRegistrationRequest {

    @NotBlank
    private String gymName;

    @NotBlank @Email
    private String ownerEmail;

    @NotBlank @Size(min = 8)
    private String password;
}