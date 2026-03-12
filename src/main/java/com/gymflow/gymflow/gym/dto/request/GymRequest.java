package com.gymflow.gymflow.gym.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GymRequest {
    @NotBlank(message = "Gym name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private String contactNumber;
}