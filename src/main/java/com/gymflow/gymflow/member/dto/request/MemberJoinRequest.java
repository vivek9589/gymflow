package com.gymflow.gymflow.member.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
@Data
public class MemberJoinRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String email;
    private String bloodGroup;
    private Double weight;
    private Double height;
    private LocalDate dob;
    private String occupation;
    private String fatherName;
    private String permanentAddress;
    private String medicalConditions;
    private Double initialPayment;

    // FIX: Variable name should be gymId (lowercase g) to match standard JSON mapping
    @NotNull(message = "Gym ID is required")
    private Long gymId;

    @NotNull(message = "Please select a membership plan")
    private Long planId;

    // ADDED: To handle the fields being sent from React
    private LocalDate startDate;
    private boolean paid;
}