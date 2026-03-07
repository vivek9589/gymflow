package com.gymflow.gymflow.dashboard.service;

import com.gymflow.gymflow.dashboard.dto.DashboardStatsDTO;

public interface DashboardService {

    DashboardStatsDTO getDashboardStats(Long gymId);
}
