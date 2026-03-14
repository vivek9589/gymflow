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
    private double revenueThisMonth;
    private double pendingPayments;
    private int pendingCount;
    private int revenueGrowth;
    private int renewalRate;
    private ExpiringSoonDTO expiringSoon; // Sub-object
    private PopularPlanDTO popularPlan;   // Sub-object
    private AttendanceHealthDTO attendanceHealth; // Sub-object
    private List<RecentMemberDTO> recentMembers;

}
