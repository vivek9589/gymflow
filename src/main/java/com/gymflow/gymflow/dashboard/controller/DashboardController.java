package com.gymflow.gymflow.dashboard.controller;


import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.dashboard.dto.DashboardStatsDTO;
import com.gymflow.gymflow.dashboard.service.impl.DashboardServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardServiceImpl dashboardService;

    @GetMapping("/stats/{gymId}")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getStats(@PathVariable Long gymId) {
        DashboardStatsDTO stats = dashboardService.getDashboardStats(gymId);
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard data fetched"));
    }
}