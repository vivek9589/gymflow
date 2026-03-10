package com.gymflow.gymflow.member.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberUpdateRequest {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String email;
    private Double weight;
    private Double height;
    private String permanentAddress;
    private String occupation;
    private String medicalConditions;
    private String status; // Allow owner to change status manually (e.g., ACTIVE/EXPIRED)
}