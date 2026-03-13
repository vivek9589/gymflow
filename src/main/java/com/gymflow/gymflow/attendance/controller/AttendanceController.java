package com.gymflow.gymflow.attendance.controller;


import com.gymflow.gymflow.attendance.dto.response.AttendanceLiveDTO;
import com.gymflow.gymflow.attendance.entity.Attendance;
import com.gymflow.gymflow.attendance.service.AttendanceService;
import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final MemberService memberService;

    @PostMapping("/scan/{gymId}/{memberId}")
    public ResponseEntity<ApiResponse<String>> scanAttendance(
            @PathVariable Long gymId,
            @PathVariable Long memberId) {
        log.info("Scan attendance request: gymId={}, memberId={}", gymId, memberId);
        String message = attendanceService.toggleAttendance(memberId, gymId);
        return ResponseEntity.ok(ApiResponse.success(null, message));
    }

    @GetMapping("/search-member")
    public ResponseEntity<ApiResponse<List<Member>>> searchMemberForManualEntry(
            @RequestParam Long gymId,
            @RequestParam String query) {
        log.info("Searching members for gymId={} with query={}", gymId, query);
        return ResponseEntity.ok(ApiResponse.success(memberService.searchMembers(gymId, query), "Members found"));
    }

    @GetMapping("/live/{gymId}")
    public ResponseEntity<ApiResponse<List<AttendanceLiveDTO>>> getLiveFeed(@PathVariable Long gymId) {
        log.info("Fetching live feed for gymId={}", gymId);
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getRecentAttendance(gymId), "Live feed fetched"));
    }

    @GetMapping("/stats/{gymId}")
    public ResponseEntity<ApiResponse<Long>> getActiveStats(@PathVariable Long gymId) {
        log.info("Fetching active stats for gymId={}", gymId);
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getActiveCount(gymId), "Active count fetched"));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<Attendance>>> getMemberAttendance(@PathVariable Long memberId) {
        log.info("Fetching attendance logs for memberId={}", memberId);
        return ResponseEntity.ok(ApiResponse.success(attendanceService.findByMemberIdOrderByCheckInTimeDesc(memberId), "Attendance logs fetched"));
    }

    @GetMapping("/today/{gymId}")
    public ResponseEntity<ApiResponse<List<Attendance>>> getTodayAttendance(@PathVariable Long gymId) {
        log.info("API call: Today's attendance for gymId={}", gymId);
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getTodayAttendance(gymId), "Today's attendance fetched"));
    }

    @GetMapping("/report/{gymId}")
    public ResponseEntity<ApiResponse<List<Attendance>>> getAttendanceReport(
            @PathVariable Long gymId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("API call: Attendance report for gymId={} from {} to {}", gymId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getAttendanceReport(gymId, startDate, endDate), "Attendance report fetched"));
    }
}