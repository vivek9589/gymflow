package com.gymflow.gymflow.dashboard.service.impl;

import com.gymflow.gymflow.attendance.repository.AttendanceRepository;
import com.gymflow.gymflow.common.exception.GymNotFoundException;
import com.gymflow.gymflow.dashboard.dto.*;
import com.gymflow.gymflow.gym.repository.GymRepository;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final AttendanceRepository attendanceRepository;

    @Override
    public DashboardStatsDTO getDashboardStats(Long gymId) {
        log.info("Fetching dashboard stats for gymId={}", gymId);

        // Validate gym existence
        gymRepository.findById(gymId)
                .orElseThrow(() -> new GymNotFoundException("Gym not found with id: " + gymId));

        // 1. Member counts (Excluding Deleted)
        long total = memberRepository.countByGymIdAndDeletedFalse(gymId);
        long active = memberRepository.countByGymIdAndStatusAndDeletedFalse(gymId, "ACTIVE");
        long expired = memberRepository.countByGymIdAndStatusAndDeletedFalse(gymId, "EXPIRED");

        // 2. Recent Members (Top 5 - Excluding Deleted)
        List<RecentMemberDTO> recentMembers = memberRepository.findTop5ByGymIdAndDeletedFalseOrderByCreatedAtDesc(gymId)
                .stream()
                .map(m -> RecentMemberDTO.builder()
                        .name(m.getName())
                        .phone(m.getPhone())
                        .planName(m.getCurrentPlan() != null ? m.getCurrentPlan().getName() : "N/A")
                        .expiryDate(m.getExpiryDate() != null ? m.getExpiryDate().toString() : "N/A")
                        .status(m.getStatus())
                        .build())
                .toList();

        // 3. Revenue this month (Includes deleted members for financial accuracy)
        BigDecimal revenue = memberRepository.calculateMonthlyRevenue(gymId, LocalDate.now().getMonthValue());

        // 4. Pending payments (Excluding Deleted)
        List<Member> activeGymMembers = memberRepository.findByGymIdAndDeletedFalse(gymId);

        BigDecimal pending = activeGymMembers.stream()
                .filter(m -> m.getCurrentPlan() != null)
                .map(m -> {
                    BigDecimal planPrice = m.getCurrentPlan().getPrice();
                    BigDecimal paid = BigDecimal.valueOf(m.getInitialPayment() != null ? m.getInitialPayment() : 0.0);
                    BigDecimal balance = planPrice.subtract(paid);
                    return balance.compareTo(BigDecimal.ZERO) > 0 ? balance : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int pendingCount = (int) activeGymMembers.stream()
                .filter(m -> m.getCurrentPlan() != null)
                .filter(m -> {
                    BigDecimal planPrice = m.getCurrentPlan().getPrice();
                    BigDecimal paid = BigDecimal.valueOf(m.getInitialPayment() != null ? m.getInitialPayment() : 0.0);
                    return planPrice.subtract(paid).compareTo(BigDecimal.ZERO) > 0;
                })
                .count();

        // 5. Expiring soon (next 7 days - Excluding Deleted)
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        List<Member> expiringSoonMembers = memberRepository.findByGymIdAndExpiryDateBetweenAndDeletedFalse(gymId, today, nextWeek);

        int expiringCount = expiringSoonMembers.size();
        double potentialRevenue = expiringSoonMembers.stream()
                .filter(m -> m.getCurrentPlan() != null)
                .mapToDouble(m -> m.getCurrentPlan().getPrice().doubleValue())
                .sum();
        ExpiringSoonDTO expiringSoon = new ExpiringSoonDTO(expiringCount, potentialRevenue);

        // 6. Popular plan (Excluding Deleted)
        PopularPlanDTO popularPlan = memberRepository.findPopularPlans(gymId, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(new PopularPlanDTO("N/A", 0L));

        // 7. Attendance health (last 7 days)
        LocalDateTime startDate = LocalDate.now().minusDays(7).atStartOfDay();
        int activeThisWeekCount = attendanceRepository.countActiveMembersThisWeek(gymId, startDate);
        int inactiveThisWeek = (int) Math.max(0, active - activeThisWeekCount);
        AttendanceHealthDTO attendanceHealth = new AttendanceHealthDTO(activeThisWeekCount, inactiveThisWeek);

        // 8. Renewal rate (renewed ÷ expired - Excluding Deleted)
        Long renewedCount = memberRepository.countByGymIdAndStatusAndDeletedFalse(gymId, "RENEWED");
        int renewalRate = expired > 0 ? (int) ((renewedCount * 100.0) / expired) : 0;

        // 9. Revenue growth (Compare current vs last month)
        BigDecimal lastMonthRevenue = memberRepository.calculateMonthlyRevenue(gymId, LocalDate.now().minusMonths(1).getMonthValue());

        int revenueGrowth = 0;
        if (lastMonthRevenue != null && lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentRevenue = (revenue != null) ? revenue : BigDecimal.ZERO;
            revenueGrowth = currentRevenue.subtract(lastMonthRevenue)
                    .divide(lastMonthRevenue, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .intValue();
        }

        return DashboardStatsDTO.builder()
                .totalMembers(total)
                .activeMembers(active)
                .expiredMembers(expired)
                .recentMembers(recentMembers)
                .revenueThisMonth(revenue != null ? revenue.doubleValue() : 0.0)
                .pendingPayments(pending.doubleValue())
                .pendingCount(pendingCount)
                .expiringSoon(expiringSoon)
                .popularPlan(popularPlan)
                .attendanceHealth(attendanceHealth)
                .renewalRate(renewalRate)
                .revenueGrowth(revenueGrowth)
                .build();
    }
}