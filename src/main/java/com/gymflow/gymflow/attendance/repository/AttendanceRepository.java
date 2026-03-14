package com.gymflow.gymflow.attendance.repository;


import com.gymflow.gymflow.attendance.dto.response.AttendanceLiveDTO;
import com.gymflow.gymflow.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = "SELECT " +
            "a.id AS id, " +
            "m.id AS memberId, " +
            "m.name AS memberName, " +
            "a.check_in_time AS checkInTime, " +
            "a.check_out_time AS checkOutTime, " +
            "DATEDIFF(m.expiry_date, CURRENT_DATE) AS daysLeft, " +
            "CASE WHEN a.check_out_time IS NULL THEN 'INSIDE' ELSE 'LEFT' END AS status, " +
            "0 AS streak " +
            "FROM attendance a " +
            "JOIN members m ON a.member_id = m.id " +
            "WHERE a.gym_id = :gymId " +
            "ORDER BY a.check_in_time DESC",
            nativeQuery = true)
    List<Object[]> findLiveTrackerData(@Param("gymId") Long gymId);


    // Count distinct active members who checked in during the last 7 days
    @Query("SELECT COUNT(DISTINCT a.member.id) " +
            "FROM Attendance a " +
            "WHERE a.gym.id = :gymId AND a.checkInTime >= :startDate")
    int countActiveMembersThisWeek(@Param("gymId") Long gymId,
                                   @Param("startDate") LocalDateTime startDate);





}