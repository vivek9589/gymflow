package com.gymflow.gymflow.member.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberJoinRequest {
    //@NotBlank(message = "Name is required")
    private String name; // [cite: 5]

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid phone number")
    private String phone; // [cite: 13]

    private String email; // [cite: 15]
    private String bloodGroup; // [cite: 18]
    private Double weight; // [cite: 22]
    private Double height; // [cite: 21]

    // Form specific fields
    private LocalDate dob; // [cite: 7]
    private String occupation; // [cite: 9]
    private String fatherName; // [cite: 11]
    private String permanentAddress; // [cite: 17]
    private String medicalConditions; // [cite: 29]
    private Double initialPayment; // [cite: 26]

   // @NotBlank(message = "Gym code is required")
    private String gymCode;

    @NotNull(message = "Please select a membership plan")
    private Long planId; // [cite: 24]
}