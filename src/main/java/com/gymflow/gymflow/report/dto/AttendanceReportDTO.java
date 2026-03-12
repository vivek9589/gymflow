package com.gymflow.gymflow.report.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Attendance report DTO showing logs and counts.
 */
@Data
@Builder
public class AttendanceReportDTO {
    private long totalSessions;
    private long activeSessions;
    private List<String> recentAttendanceLogs; // e.g., "Rahul checked in at 10:00 AM"
}