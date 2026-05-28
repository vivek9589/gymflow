package com.gymflow.gymflow.dashboard.service.impl;

import com.gymflow.gymflow.attendance.repository.AttendanceRepository;
import com.gymflow.gymflow.common.exception.GymNotFoundException;
import com.gymflow.gymflow.dashboard.dto.*;
import com.gymflow.gymflow.gym.repository.GymRepository;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.dashboard.service.DashboardService;
import com.gymflow.gymflow.payment.entity.Payment;
import com.gymflow.gymflow.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final MemberRepository memberRepository;
    private final GymRepository gymRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository; // Injected Ledger Repository

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats(Long gymId) {
        log.info("Fetching transactionally accurate dashboard stats for gymId={}", gymId);

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

        // 3. Date boundary ranges for this month vs last month
        LocalDate todayDate = LocalDate.now();
        LocalDateTime currentMonthStart = todayDate.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime currentMonthEnd = todayDate.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        LocalDateTime lastMonthStart = todayDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime lastMonthEnd = todayDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        // 4. Ledger-derived aggregate monthly revenue
        BigDecimal revenue = paymentRepository.calculateRevenueForPeriod(gymId, currentMonthStart, currentMonthEnd);

        // 5. Pending payments calculation using transactional ledger values
        List<Member> activeGymMembers = memberRepository.findByGymIdAndDeletedFalse(gymId);

        BigDecimal pending = BigDecimal.ZERO;
        int pendingCount = 0;

        for (Member m : activeGymMembers) {
            if (m.getCurrentPlan() != null && m.getSubscriptionStartDate() != null) {
                BigDecimal planPrice = m.getCurrentPlan().getPrice();

                // Fetch payments registered for this member's current active plan window
                LocalDateTime cycleStart = m.getSubscriptionStartDate().atStartOfDay();

                List<Payment> cyclePayments = paymentRepository.findByMemberId(m.getId());
                BigDecimal totalPaidInCycle = cyclePayments.stream()
                        .filter(p -> "SUCCESS".equalsIgnoreCase(p.getStatus()))
                        .filter(p -> !p.getCreatedAt().isBefore(cycleStart))
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal balance = planPrice.subtract(totalPaidInCycle);
                if (balance.compareTo(BigDecimal.ZERO) > 0) {
                    pending = pending.add(balance);
                    pendingCount++;
                }
            }
        }

        // 6. Expiring soon (next 7 days)
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        List<Member> expiringSoonMembers = memberRepository
                .findByGymIdAndExpiryDateBetweenAndDeletedFalse(gymId, today, nextWeek);

        int expiringCount = expiringSoonMembers.size();
        double potentialRevenue = expiringSoonMembers.stream()
                .filter(m -> m.getCurrentPlan() != null)
                .mapToDouble(m -> m.getCurrentPlan().getPrice().doubleValue())
                .sum();

        List<ExpiringMemberDTO> expiringSoonDTOs = expiringSoonMembers.stream()
                .map(m -> ExpiringMemberDTO.builder()
                        .name(m.getName())
                        .phone(m.getPhone())
                        .planName(m.getCurrentPlan() != null ? m.getCurrentPlan().getName() : "N/A")
                        .expiryDate(m.getExpiryDate() != null ? m.getExpiryDate().toString() : "N/A")
                        .status(m.getStatus())
                        .daysLeft(m.getExpiryDate() != null ? ChronoUnit.DAYS.between(LocalDate.now(), m.getExpiryDate()) : 0)
                        .build())
                .toList();

        ExpiringSoonDTO expiringSoon = new ExpiringSoonDTO(expiringCount, potentialRevenue, expiringSoonDTOs);

        // 7. Popular plan (Excluding Deleted)
        PopularPlanDTO popularPlan = memberRepository.findPopularPlans(gymId, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(new PopularPlanDTO("N/A", 0L));

        // 8. Attendance health
        LocalDateTime startDate = LocalDate.now().minusDays(7).atStartOfDay();
        int activeThisWeekCount = attendanceRepository.countActiveMembersThisWeek(gymId, startDate);
        int inactiveThisWeek = (int) Math.max(0, active - activeThisWeekCount);
        AttendanceHealthDTO attendanceHealth = new AttendanceHealthDTO(activeThisWeekCount, inactiveThisWeek);

        // 9. Renewal rate
        Long renewedCount = memberRepository.countByGymIdAndStatusAndDeletedFalse(gymId, "RENEWED");
        int renewalRate = expired > 0 ? (int) ((renewedCount * 100.0) / expired) : 0;

        // 10. Ledger-derived historical growth trends
        BigDecimal lastMonthRevenue = paymentRepository.calculateRevenueForPeriod(gymId, lastMonthStart, lastMonthEnd);

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