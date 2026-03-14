package com.gymflow.gymflow.report.controller;

import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.report.dto.*;
import com.gymflow.gymflow.report.service.ReportsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/revenue/{gymId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<RevenueReportDTO>> getRevenueReport(
            @PathVariable Long gymId,
            @RequestParam int year) {
        log.info("API call: Revenue report for gymId={} year={}", gymId, year);
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getRevenueReport(gymId, year), "Revenue report fetched"));
    }

    @GetMapping("/members/{gymId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MemberReportDTO>> getMemberReport(@PathVariable Long gymId) {
        log.info("API call: Member report for gymId={}", gymId);
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getMemberReport(gymId), "Member report fetched"));
    }

    @GetMapping("/attendance/{gymId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<AttendanceReportDTO>> getAttendanceReport(@PathVariable Long gymId) {
        log.info("API call: Attendance report for gymId={}", gymId);
        return ResponseEntity.ok(ApiResponse.success(
                reportsService.getAttendanceReport(gymId), "Attendance report fetched"));
    }

    /**
     * Unified endpoint to download Excel reports.
     * Path: /api/reports/download/attendance/5
     * Path: /api/reports/download/revenue/5?year=2026
     */
    @GetMapping("/download/{type}/{gymId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<byte[]> downloadExcelReport(
            @PathVariable String type,
            @PathVariable Long gymId,
            @RequestParam(required = false, defaultValue = "2026") int year) {

        log.info("Request to download {} report for gymId: {}", type, gymId);

        byte[] fileContent;
        String fileName;

        // Route to the correct service method based on type
        switch (type.toLowerCase()) {
            case "attendance":
                fileContent = reportsService.generateAttendanceExcel(gymId);
                fileName = "Attendance_Report_" + gymId + ".xlsx";
                break;
            case "members":
                fileContent = reportsService.generateMemberExcel(gymId);
                fileName = "Member_Stats_" + gymId + ".xlsx";
                break;
            case "revenue":
                fileContent = reportsService.generateRevenueExcel(gymId, year);
                fileName = "Revenue_Report_" + year + "_Gym_" + gymId + ".xlsx";
                break;
            default:
                log.warn("Invalid report type requested: {}", type);
                return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(fileContent);
    }
}