package com.gymflow.gymflow.gym.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GymDTO {
    private Long id;

    @NotBlank(message = "Gym name is required")
    private String name;

    private String gymCode; // Read-only for the owner usually

    @NotBlank(message = "Address is required")
    private String address;

    private String contactNumber;
}