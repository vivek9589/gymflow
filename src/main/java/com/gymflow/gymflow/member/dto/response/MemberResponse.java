package com.gymflow.gymflow.member.dto.response;


import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String bloodGroup;
    private Double weight;
    private Double height;
    private String occupation;
    private String permanentAddress;
    private String medicalConditions;
    private String status;
    private String planName;
    private LocalDate registrationDate;
    private LocalDate expiryDate;
    private Double initialPayment;
}
