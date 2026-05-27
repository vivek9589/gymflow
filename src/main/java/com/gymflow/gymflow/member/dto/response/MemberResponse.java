package com.gymflow.gymflow.member.dto.response;

import lombok.*;
import java.math.BigDecimal; // Add this import
import java.time.LocalDate;
import java.util.List;

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
    private BigDecimal initialPayment; // Changed from Double to BigDecimal
    private String checkInToken;

    // 🔑 New fields for renewal & history
    private LocalDate nextDueDate;
    private String digitalAccessPassLink;
    private List<PaymentHistoryDTO> paymentHistory;
    private List<RenewalOptionDTO> renewalOptions;
    // Add this field to your existing MemberResponse.java
    private List<SubscriptionHistoryDTO> planHistory;

}