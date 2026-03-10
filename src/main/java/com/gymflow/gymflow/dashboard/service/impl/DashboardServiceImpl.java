package com.gymflow.gymflow.dashboard.service.impl;


import com.gymflow.gymflow.dashboard.dto.DashboardStatsDTO;

import com.gymflow.gymflow.dashboard.dto.RecentMemberDTO;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

        // 3. Revenue Calculation (Actual payments received this month)
        BigDecimal revenue = memberRepository.calculateMonthlyRevenue(gymId, LocalDate.now().getMonthValue());

        // 4. DYNAMIC Pending Payments Calculation
        // Logic: For all members in this gym, find the difference between their Plan Price and what they paid initially.
        List<Member> allGymMembers = memberRepository.findByGymId(gymId);

        BigDecimal pending = allGymMembers.stream()
                .filter(m -> m.getCurrentPlan() != null) // Only members with a plan
                .map(m -> {
                    BigDecimal planPrice = m.getCurrentPlan().getPrice(); // Total cost
                    BigDecimal paid = BigDecimal.valueOf(m.getInitialPayment() != null ? m.getInitialPayment() : 0.0);
                    BigDecimal balance = planPrice.subtract(paid);
                    return balance.compareTo(BigDecimal.ZERO) > 0 ? balance : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardStatsDTO.builder()
                .totalMembers(total)
                .activeMembers(active)
                .expiredMembers(expired)
                .recentMembers(recentMembers)
                .revenueThisMonth(revenue != null ? revenue : BigDecimal.ZERO)
                .pendingPayments(pending) // Now dynamic based on member data
                .build();
    }
}