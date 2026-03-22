package com.gymflow.gymflow.dashboard.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExpiringMemberDTO {
    private String name;
    private String phone;
    private String planName;
    private String expiryDate;
    private String status;
    private long daysLeft; // optional, for “expires in X days”
}