package com.gymflow.gymflow.member.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@NoArgsConstructor
@AllArgsConstructor // Gives @Builder the exact constructor it needs for all fields
@Builder
public class MemberListResponseDto {

    @Getter private Long id;
    @Getter private String name;
    @Getter private String phone;
    @Getter private String email;
    @Getter private String currentPlan;
    @Getter private LocalDate expiryDate;
    @Getter private LocalDateTime createdAt;

    private String status;

    // Custom getter that overrides the status field serialization dynamically
    public String getStatus() {
        if (this.expiryDate != null && this.expiryDate.isBefore(LocalDate.now())) {
            return "EXPIRED";
        }
        return this.status;
    }
}