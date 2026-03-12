package com.gymflow.gymflow.report.dto;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Revenue report DTO showing monthly/annual breakdown.
 */
@Data
@Builder
public class RevenueReportDTO {
    private BigDecimal totalRevenue;
    private Map<String, BigDecimal> monthlyRevenue; // e.g., {"Jan": 12000, "Feb": 15000}
}