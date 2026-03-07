package com.gymflow.gymflow.attendance.service.impl;

import com.gymflow.gymflow.attendance.entity.Attendance;
import com.gymflow.gymflow.attendance.repository.AttendanceRepository;
import com.gymflow.gymflow.attendance.service.AttendanceService;
import com.gymflow.gymflow.common.exception.BusinessException;
import com.gymflow.gymflow.gym.repository.GymRepository;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private final GymRepository gymRepository;

    @Override
    @Transactional
    public String toggleAttendance(Long memberId, Long gymId) {
        // 1. Validate Member
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("Member not found"));

        // 2. Validate Gym Ownership (Security Check)
        if (!member.getGym().getId().equals(gymId)) {
            throw new BusinessException("Member does not belong to this Gym");
        }

        // 3. Status Check
        if ("EXPIRED".equalsIgnoreCase(member.getStatus())) {
            throw new BusinessException("Membership expired. Access Denied.");
        }

        if ("PENDING".equalsIgnoreCase(member.getStatus())) {
            throw new BusinessException("Admission pending. Please contact the manager.");
        }

        // 4. Toggle Session Logic
        Optional<Attendance> activeSession = attendanceRepository
                .findFirstByMemberIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(memberId);

        if (activeSession.isPresent()) {
            Attendance attendance = activeSession.get();
            attendance.setCheckOutTime(LocalDateTime.now());
            attendanceRepository.save(attendance);
            return "Check-out successful at " + attendance.getCheckOutTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        } else {
            Attendance attendance = new Attendance();
            attendance.setMember(member);
            attendance.setGym(member.getGym());
            attendance.setCheckInTime(LocalDateTime.now());
            attendanceRepository.save(attendance);
            return "Check-in successful at " + attendance.getCheckInTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        }
    }

    @Override
    public List<Attendance> getRecentAttendance(Long gymId) {
        return attendanceRepository.findByGymIdOrderByCheckInTimeDesc(gymId);
    }


    @Override
    public long getActiveCount(Long gymId) {
        // We filter the recent list to see who hasn't checked out
        return attendanceRepository.findByGymIdOrderByCheckInTimeDesc(gymId)
                .stream()
                .filter(a -> a.getCheckOutTime() == null)
                .count();
    }
}