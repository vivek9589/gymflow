package com.gymflow.gymflow.dashboard.service.impl;

import com.gymflow.gymflow.common.exception.GymNotFoundException;
import com.gymflow.gymflow.dashboard.dto.DashboardStatsDTO;
import com.gymflow.gymflow.dashboard.dto.RecentMemberDTO;
import com.gymflow.gymflow.gym.repository.GymRepository;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for dashboard statistics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final MemberRepository memberRepository;
    private final GymRepository gymRepository;

    @Override
    public DashboardStatsDTO getDashboardStats(Long gymId) {
        log.info("Fetching dashboard stats for gymId={}", gymId);

        // Validate gym existence
        gymRepository.findById(gymId)
                .orElseThrow(() -> new GymNotFoundException("Gym not found with id: " + gymId));

        // 1. Member counts
        long total = memberRepository.countByGymId(gymId);
        long active = memberRepository.countByGymIdAndStatus(gymId, "ACTIVE");
        long expired = memberRepository.countByGymIdAndStatus(gymId, "EXPIRED");

        // 2. Recent Members (Top 5)
        List<RecentMemberDTO> recentMembers = memberRepository.findTop5ByGymIdOrderByCreatedAtDesc(gymId)
                .stream()
                .map(m -> RecentMemberDTO.builder()
                        .name(m.getName())
                        .phone(m.getPhone())
                        .planName(m.getCurrentPlan() != null ? m.getCurrentPlan().getName() : "N/A")
                        .expiryDate(m.getExpiryDate() != null ? m.getExpiryDate().toString() : "N/A")
                        .status(m.getStatus())
                        .build())
                .collect(Collectors.toList());

        // 3. Revenue this month
        BigDecimal revenue = memberRepository.calculateMonthlyRevenue(gymId, LocalDate.now().getMonthValue());

        // 4. Pending payments
        List<Member> allGymMembers = memberRepository.findByGymId(gymId);
        BigDecimal pending = allGymMembers.stream()
                .filter(m -> m.getCurrentPlan() != null)
                .map(m -> {
                    BigDecimal planPrice = m.getCurrentPlan().getPrice();
                    BigDecimal paid = BigDecimal.valueOf(m.getInitialPayment() != null ? m.getInitialPayment() : 0.0);
                    BigDecimal balance = planPrice.subtract(paid);
                    return balance.compareTo(BigDecimal.ZERO) > 0 ? balance : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Dashboard stats calculated: total={}, active={}, expired={}, revenue={}, pending={}",
                total, active, expired, revenue, pending);

        return DashboardStatsDTO.builder()
                .totalMembers(total)
                .activeMembers(active)
                .expiredMembers(expired)
                .recentMembers(recentMembers)
                .revenueThisMonth(revenue != null ? revenue : BigDecimal.ZERO)
                .pendingPayments(pending)
                .build();
    }
}