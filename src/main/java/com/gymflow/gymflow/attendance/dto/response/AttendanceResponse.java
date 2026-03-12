package com.gymflow.gymflow.attendance.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceResponse {
    private Long id;
    private String memberName;
    private String gymName;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
}