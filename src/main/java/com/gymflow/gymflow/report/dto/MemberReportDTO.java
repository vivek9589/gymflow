package com.gymflow.gymflow.report.dto;


import lombok.Builder;
import lombok.Data;

/**
 * Member report DTO showing counts and breakdown.
 */
@Data
@Builder
public class MemberReportDTO {
    private long totalMembers;
    private long activeMembers;
    private long expiredMembers;
    private long pendingMembers;
}