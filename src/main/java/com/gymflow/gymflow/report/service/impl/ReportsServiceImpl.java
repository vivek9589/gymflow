package com.gymflow.gymflow.report.service.impl;


import com.gymflow.gymflow.common.exception.GymNotFoundException;
import com.gymflow.gymflow.gym.repository.GymRepository;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.attendance.repository.AttendanceRepository;
import com.gymflow.gymflow.report.dto.*;
import com.gymflow.gymflow.report.service.ReportsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportsServiceImpl implements ReportsService {

    private final GymRepository gymRepository;
    private final MemberRepository memberRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    public RevenueReportDTO getRevenueReport(Long gymId, int year) {
        log.info("Fetching revenue report for gymId={} year={}", gymId, year);
        gymRepository.findById(gymId)
                .orElseThrow(() -> new GymNotFoundException("Gym not found with id: " + gymId));

        // Example: fetch monthly revenue from repository
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal revenue = memberRepository.calculateMonthlyRevenue(gymId, month);
            monthlyRevenue.put(Month.of(month).name(), revenue != null ? revenue : BigDecimal.ZERO);
        }

        BigDecimal totalRevenue = monthlyRevenue.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RevenueReportDTO.builder()
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .build();
    }

    @Override
    public MemberReportDTO getMemberReport(Long gymId) {
        log.info("Fetching member report for gymId={}", gymId);
        gymRepository.findById(gymId)
                .orElseThrow(() -> new GymNotFoundException("Gym not found with id: " + gymId));

        long total = memberRepository.countByGymId(gymId);
        long active = memberRepository.countByGymIdAndStatus(gymId, "ACTIVE");
        long expired = memberRepository.countByGymIdAndStatus(gymId, "EXPIRED");
        long pending = memberRepository.countByGymIdAndStatus(gymId, "PENDING");

        return MemberReportDTO.builder()
                .totalMembers(total)
                .activeMembers(active)
                .expiredMembers(expired)
                .pendingMembers(pending)
                .build();
    }

    @Override
    public AttendanceReportDTO getAttendanceReport(Long gymId) {
        log.info("Fetching attendance report for gymId={}", gymId);
        gymRepository.findById(gymId)
                .orElseThrow(() -> new GymNotFoundException("Gym not found with id: " + gymId));

        long totalSessions = attendanceRepository.countByGymId(gymId);
        long activeSessions = attendanceRepository.countByGymIdAndCheckOutTimeIsNull(gymId);

        List<String> recentLogs = attendanceRepository.findByGymIdOrderByCheckInTimeDesc(gymId)
                .stream()
                .limit(5)
                .map(a -> a.getMember().getName() + " checked in at " + a.getCheckInTime())
                .toList();

        return AttendanceReportDTO.builder()
                .totalSessions(totalSessions)
                .activeSessions(activeSessions)
                .recentAttendanceLogs(recentLogs)
                .build();
    }
}