package com.gymflow.gymflow.attendance.service.impl;

import com.gymflow.gymflow.attendance.dto.response.AttendanceLiveDTO;
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

import java.sql.Timestamp;
import java.time.LocalDate;
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

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new GymNotFoundException("Gym not found with id: " + gymId));

        if (!member.getGym().getId().equals(gymId)) {
            log.warn("Member {} does not belong to gym {}", memberId, gymId);
            throw new GymNotFoundException("Member does not belong to this gym");
        }

        if ("EXPIRED".equalsIgnoreCase(member.getStatus())) {
            throw new MembershipExpiredException("Membership expired! Access denied.");
        }

        if ("PENDING".equalsIgnoreCase(member.getStatus())) {
            throw new AdmissionPendingException("Admission pending. Please check payment status.");
        }

        // Search for an active session that started TODAY to prevent lingering multi-day sign-ins
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        Optional<Attendance> activeSession = attendanceRepository.findFirstByMemberIdAndCheckInTimeAfterAndCheckOutTimeIsNullOrderByCheckInTimeDesc(memberId, startOfToday);

        if (activeSession.isPresent()) {
            Attendance attendance = activeSession.get();
            attendance.setCheckOutTime(LocalDateTime.now());
            attendanceRepository.save(attendance);
            return "Check-out successful at " +
                    attendance.getCheckOutTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        } else {
            Attendance attendance = Attendance.builder()
                    .member(member)
                    .gym(gym)
                    .checkInTime(LocalDateTime.now())
                    .build();
            attendanceRepository.save(attendance);
            return "Check-in successful at " +
                    attendance.getCheckInTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
        }
    }

    @Override
    public List<AttendanceLiveDTO> getRecentAttendance(Long gymId) {
        log.debug("Fetching recent attendance feed for gymId={}", gymId);

        // PERFORMANCE IMPROVEMENT: Your repository query should internally filter
        // to only fetch records where checkInTime >= TODAY.
        List<Object[]> rows = attendanceRepository.findLiveTrackerData(gymId);

        return rows.stream()
                .map(r -> new AttendanceLiveDTO(
                        ((Number) r[0]).longValue(),
                        ((Number) r[1]).longValue(),
                        (String) r[2],
                        (r[3] != null ? ((Timestamp) r[3]).toLocalDateTime() : null),
                        (r[4] != null ? ((Timestamp) r[4]).toLocalDateTime() : null),
                        r[5],
                        (String) r[6],
                        ((Number) r[7]).intValue()
                ))
                .toList();
    }

    @Override
    public long getActiveCount(Long gymId) {
        log.info("Fetching active count for gymId={}", gymId);
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        return attendanceRepository.countByGymIdAndCheckOutTimeIsNullAndCheckInTimeAfter(gymId, startOfToday);
    }


    @Override
    public List<Attendance> findByMemberIdOrderByCheckInTimeDesc(Long memberId) {
        return attendanceRepository.findByMemberIdOrderByCheckInTimeDesc(memberId);
    }

    @Override
    public List<Attendance> getTodayAttendance(Long gymId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        return attendanceRepository.findByGymIdAndCheckInTimeBetweenOrderByCheckInTimeDesc(gymId, startOfDay, endOfDay);
    }

    @Override
    public List<Attendance> getAttendanceReport(Long gymId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return attendanceRepository.findByGymIdAndCheckInTimeBetweenOrderByCheckInTimeDesc(gymId, start, end);
    }

    @Transactional
    public String processSelfToggleAttendance(
            String token,
            double memberLat,
            double memberLon
    ) {

        log.info("Attendance process started");

        // =====================================================
        // FETCH MEMBER
        // =====================================================

        Member member = memberRepository.findByCheckInToken(token)
                .orElseThrow(() -> {

                    log.warn("Invalid access token -> {}", token);

                    return new InvalidAccessPassException(
                            "Invalid or corrupted access pass link."
                    );
                });

        Gym gym = member.getGym();

        // =====================================================
        // VALIDATE GYM
        // =====================================================

        if (gym == null) {

            log.error(
                    "Gym missing for member -> {}",
                    member.getId()
            );

            throw new GymConfigurationException(
                    "Your profile is not assigned to an active gym."
            );
        }

        // =====================================================
        // VALIDATE MEMBERSHIP
        // =====================================================

        if (member.getExpiryDate() == null) {

            log.error(
                    "Membership expiry date missing -> member {}",
                    member.getId()
            );

            throw new GymConfigurationException(
                    "Membership validity unavailable."
            );
        }

        if (member.getExpiryDate().isBefore(LocalDate.now())) {

            log.warn(
                    "Membership expired -> {}",
                    member.getName()
            );

            throw new MembershipExpiredException(
                    "Access Denied: Membership expired on "
                            + member.getExpiryDate()
            );
        }

        // =====================================================
        // VALIDATE GYM GPS
        // =====================================================

        if (gym.getLatitude() == null || gym.getLongitude() == null) {

            log.error(
                    "Gym GPS not configured -> gym {}",
                    gym.getId()
            );

            throw new GymConfigurationException(
                    "Gym location services unavailable."
            );
        }

        // =====================================================
        // DISTANCE CALCULATION
        // =====================================================

        double distance = calculateHaversineDistance(
                memberLat,
                memberLon,
                gym.getLatitude(),
                gym.getLongitude()
        );

        log.info(
                "Distance Calculated -> {} meters",
                distance
        );

        // Increased radius for mobile GPS inaccuracies
        if (distance > 150.0) {

            log.warn(
                    "Geofence blocked -> member {} distance {}m",
                    member.getName(),
                    distance
            );

            throw new GeofenceViolationException(
                    "Access Denied! You must be inside the gym premises."
            );
        }

        // =====================================================
        // FIND ACTIVE SESSION
        // =====================================================

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        Optional<Attendance> activeSession =
                attendanceRepository
                        .findFirstByMemberIdAndCheckInTimeAfterAndCheckOutTimeIsNullOrderByCheckInTimeDesc(
                                member.getId(),
                                startOfToday
                        );

        // =====================================================
        // CHECK OUT
        // =====================================================

        if (activeSession.isPresent()) {

            Attendance attendance = activeSession.get();

            attendance.setCheckOutTime(LocalDateTime.now());

            attendanceRepository.save(attendance);

            log.info(
                    "CHECK-OUT SUCCESS -> {}",
                    member.getName()
            );

            return "Goodbye "
                    + member.getName()
                    + "! Checked out successfully.";
        }

        // =====================================================
        // CHECK IN
        // =====================================================

        Attendance attendance = new Attendance();

        attendance.setMember(member);
        attendance.setGym(gym);
        attendance.setCheckInTime(LocalDateTime.now());

        attendanceRepository.save(attendance);

        log.info(
                "CHECK-IN SUCCESS -> {}",
                member.getName()
        );

        return "Welcome "
                + member.getName()
                + "! Checked in successfully.";
    }

    // Mathematical formula to calculate distance in meters between two GPS coordinates
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000; // Meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

}