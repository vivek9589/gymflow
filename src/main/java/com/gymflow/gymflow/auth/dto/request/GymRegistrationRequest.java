package com.gymflow.gymflow.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for registering a new Gym along with its Owner.
 * This is used when a gym owner signs up for the first time.
 */
@Data
public class GymRegistrationRequest {

    @NotBlank(message = "Gym name is required")
    private String gymName;

    @NotBlank(message = "Owner email is required")
    @Email(message = "Invalid email format")
    private String ownerEmail;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}