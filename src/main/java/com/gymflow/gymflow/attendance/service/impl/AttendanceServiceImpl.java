package com.gymflow.gymflow.attendance.service.impl;

import com.gymflow.gymflow.attendance.entity.Attendance;
import com.gymflow.gymflow.attendance.repository.AttendanceRepository;
import com.gymflow.gymflow.attendance.service.AttendanceService;
import com.gymflow.gymflow.common.exception.*;
import com.gymflow.gymflow.gym.entity.Gym;
import com.gymflow.gymflow.gym.repository.GymRepository;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final GymRepository gymRepository;

    @Override
    @Transactional
    public String toggleAttendance(Long memberId, Long gymId) {
        log.info("Toggling attendance for memberId={} at gymId={}", memberId, gymId);

        // Validate Member
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        // Validate Gym
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new GymNotFoundException("Gym not found with id: " + gymId));

        // Ensure member belongs to this gym
        if (!member.getGym().getId().equals(gymId)) {
            log.warn("Member {} does not belong to gym {}", memberId, gymId);
            throw new GymNotFoundException("Member does not belong to this gym");
        }

        // Membership status checks
        if ("EXPIRED".equalsIgnoreCase(member.getStatus())) {
            log.warn("Member {} attempted check-in with expired membership", memberId);
            throw new MembershipExpiredException("Membership expired. Access denied.");
        }

        if ("PENDING".equalsIgnoreCase(member.getStatus())) {
            log.warn("Member {} attempted check-in with pending admission", memberId);
            throw new AdmissionPendingException("Admission pending. Please contact the manager.");
        }

        // Toggle session logic
        Optional<Attendance> activeSession = attendanceRepository
                .findFirstByMemberIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(memberId);

        if (activeSession.isPresent()) {
            Attendance attendance = activeSession.get();
            attendance.setCheckOutTime(LocalDateTime.now());
            attendanceRepository.save(attendance);
            log.info("Member {} checked out at {}", member.getName(), attendance.getCheckOutTime());
            return "Check-out successful at " +
                    attendance.getCheckOutTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        } else {
            Attendance attendance = Attendance.builder()
                    .member(member)
                    .gym(member.getGym())
                    .checkInTime(LocalDateTime.now())
                    .build();
            attendanceRepository.save(attendance);
            log.info("Member {} checked in at {}", member.getName(), attendance.getCheckInTime());
            return "Check-in successful at " +
                    attendance.getCheckInTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        }
    }

    @Override
    public List<Attendance> getRecentAttendance(Long gymId) {
        log.info("Fetching recent attendance for gymId={}", gymId);
        return attendanceRepository.findByGymIdOrderByCheckInTimeDesc(gymId);
    }

    @Override
    public long getActiveCount(Long gymId) {
        log.info("Fetching active count for gymId={}", gymId);
        return attendanceRepository.findByGymIdOrderByCheckInTimeDesc(gymId)
                .stream()
                .filter(a -> a.getCheckOutTime() == null)
                .count();
    }

    @Override
    public List<Attendance> findByMemberIdOrderByCheckInTimeDesc(Long memberId) {
        log.info("Fetching attendance logs for memberId={}", memberId);
        return attendanceRepository.findByMemberIdOrderByCheckInTimeDesc(memberId);
    }
}