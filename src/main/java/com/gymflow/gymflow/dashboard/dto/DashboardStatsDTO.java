package com.gymflow.gymflow.dashboard.dto;



import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardStatsDTO {
    private long totalMembers;
    private long activeMembers;
    private long expiredMembers;
    private BigDecimal revenueThisMonth;
    private BigDecimal pendingPayments;
    private List<RecentMemberDTO> recentMembers;
}

