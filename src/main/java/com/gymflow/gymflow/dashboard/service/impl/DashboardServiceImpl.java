package com.gymflow.gymflow.dashboard.service.impl;


import com.gymflow.gymflow.dashboard.dto.DashboardStatsDTO;

import com.gymflow.gymflow.dashboard.dto.RecentMemberDTO;
import com.gymflow.gymflow.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl {

    private final MemberRepository memberRepository;

    public DashboardStatsDTO getDashboardStats(Long gymId) {
        // 1. Get counts using optimized Repository methods
        long total = memberRepository.countByGymId(gymId);
        long active = memberRepository.countByGymIdAndStatus(gymId, "ACTIVE");
        long expired = memberRepository.countByGymIdAndStatus(gymId, "EXPIRED");

        // 2. Fetch Recent Members (Top 5)
        var recentMembers = memberRepository.findTop5ByGymIdOrderByCreatedAtDesc(gymId)
                .stream()
                .map(m -> RecentMemberDTO.builder()
                        .name(m.getName())
                        .phone(m.getPhone())
                        .planName(m.getCurrentPlan() != null ? m.getCurrentPlan().getName() : "N/A")
                        .expiryDate(m.getExpiryDate().toString())
                        .status(m.getStatus())
                        .build())
                .collect(Collectors.toList());

        // 3. Simple Revenue Logic (Sum of active plan prices for this month)
        // Note: Real-world apps use a separate 'Payments' table for this
        BigDecimal revenue = memberRepository.calculateMonthlyRevenue(gymId, LocalDate.now().getMonthValue());

        return DashboardStatsDTO.builder()
                .totalMembers(total)
                .activeMembers(active)
                .expiredMembers(expired)
                .recentMembers(recentMembers)
                .revenueThisMonth(revenue != null ? revenue : BigDecimal.ZERO)
                .pendingPayments(BigDecimal.valueOf(18000)) // Static for now, or calculate based on logic
                .build();
    }
}