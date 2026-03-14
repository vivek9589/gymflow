package com.gymflow.gymflow.report.service;

import com.gymflow.gymflow.report.dto.RevenueReportDTO;
import com.gymflow.gymflow.report.dto.MemberReportDTO;
import com.gymflow.gymflow.report.dto.AttendanceReportDTO;


public interface ReportsService {
    // Keep your existing DTO methods for the dashboard
    RevenueReportDTO getRevenueReport(Long gymId, int year);
    MemberReportDTO getMemberReport(Long gymId);
    AttendanceReportDTO getAttendanceReport(Long gymId);

    // Add Excel generation methods
    byte[] generateRevenueExcel(Long gymId, int year);
    byte[] generateMemberExcel(Long gymId);
    byte[] generateAttendanceExcel(Long gymId);
}