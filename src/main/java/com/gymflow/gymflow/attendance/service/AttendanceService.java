package com.gymflow.gymflow.attendance.service;


import com.gymflow.gymflow.attendance.entity.Attendance;

import java.util.List;

public interface AttendanceService {
    String toggleAttendance(Long memberId, Long gymId);
    List<Attendance> getRecentAttendance(Long gymId);

    // Add this for your KPI cards
    long getActiveCount(Long gymId);
}