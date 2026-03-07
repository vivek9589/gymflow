package com.gymflow.gymflow.auth.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OwnerRegisterRequest {

    // Owner details
    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Gym details
    @NotBlank(message = "Gym name is required")
    private String gymName;

    @NotBlank(message = "Gym address is required")
    private String address;

    private String contactNumber;

    private String city;
    private String state;
    private String pincode;

    private String website;
    private Integer establishedYear;

    private String logoUrl;
    private String description;

    // Optional role field (default OWNER)
    private String role = "OWNER";
}
