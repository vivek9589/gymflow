package com.gymflow.gymflow.report.controller;

import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.report.dto.*;
import com.gymflow.gymflow.report.service.ReportsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}