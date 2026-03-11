package com.gymflow.gymflow.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request DTO for updating a Gym Owner's profile.
 * Only exposes editable fields, not sensitive ones like password.
 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    private String contactNumber;
    private String city;
    private String state;
    private String pincode;
    private String website;
    private String description;
    private String logoUrl;
}