package com.gymflow.gymflow.member.dto.response;

import lombok.Builder;
import lombok.Data;


import java.time.LocalDate;
@Data
@Builder
public class SubscriptionHistoryDTO {
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}
