package com.gymflow.gymflow.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecentMemberDTO {
    private String name;
    private String phone;
    private String planName;
    private String expiryDate;
    private String status;
}