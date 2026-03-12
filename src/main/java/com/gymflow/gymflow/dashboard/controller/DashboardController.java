package com.gymflow.gymflow.dashboard.controller;

import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.dashboard.dto.DashboardStatsDTO;
import com.gymflow.gymflow.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats/{gymId}")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getStats(@PathVariable Long gymId) {
        log.info("API call: Fetch dashboard stats for gymId={}", gymId);
        DashboardStatsDTO stats = dashboardService.getDashboardStats(gymId);
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard data fetched"));
    }
}