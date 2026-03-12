package com.gymflow.gymflow.attendance.repository;


import com.gymflow.gymflow.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Find the last record for a member that hasn't checked out yet
    Optional<Attendance> findFirstByMemberIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(Long memberId);

    List<Attendance> findByGymIdOrderByCheckInTimeDesc(Long gymId);

    List<Attendance> findByMemberIdOrderByCheckInTimeDesc(Long memberId);

    List<Attendance> findByGymIdAndCheckInTimeBetweenOrderByCheckInTimeDesc(
            Long gymId, LocalDateTime start, LocalDateTime end);

    // Count all attendance records for a gym
    long countByGymId(Long gymId);

    // Count active sessions (check-out time is null)
    long countByGymIdAndCheckOutTimeIsNull(Long gymId);




}