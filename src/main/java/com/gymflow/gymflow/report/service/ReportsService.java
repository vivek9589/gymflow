package com.gymflow.gymflow.report.service;

import com.gymflow.gymflow.report.dto.RevenueReportDTO;
import com.gymflow.gymflow.report.dto.MemberReportDTO;
import com.gymflow.gymflow.report.dto.AttendanceReportDTO;

public interface ReportsService {
    RevenueReportDTO getRevenueReport(Long gymId, int year);
    MemberReportDTO getMemberReport(Long gymId);
    AttendanceReportDTO getAttendanceReport(Long gymId);
}