package com.gymflow.gymflow.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;



import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttendanceLiveDTO {
    private Long id;
    private Long memberId;
    private String memberName;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Long daysLeft;
    private String status;
    private Integer streak;

    // Hibernate uses this constructor for the "SELECT new" query
    public AttendanceLiveDTO(Long id, Long memberId, String memberName,
                             LocalDateTime checkInTime, LocalDateTime checkOutTime,
                             Object daysLeft, String status, Integer streak) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        // Safely convert the daysLeft object to Long
        this.daysLeft = (daysLeft instanceof Number) ? ((Number) daysLeft).longValue() : 0L;
        this.status = status;
        this.streak = (streak instanceof Number) ? ((Number) streak).intValue() : 0;
    }
}