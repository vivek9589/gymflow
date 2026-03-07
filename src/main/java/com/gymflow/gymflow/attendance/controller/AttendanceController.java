package com.gymflow.gymflow.attendance.controller;


import com.gymflow.gymflow.attendance.entity.Attendance;
import com.gymflow.gymflow.attendance.service.AttendanceService;
import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final MemberService memberService; // Inject this to handle the manual search

    // 1. MANUAL & QR TOGGLE: The "Action" button
    // This is used for both QR scans and the "Check In" button in your manual search
    @PostMapping("/scan/{gymId}/{memberId}")
    public ResponseEntity<ApiResponse<String>> scanAttendance(
            @PathVariable Long gymId,
            @PathVariable Long memberId) {

        // The service now handles logic using gymId
        String message = attendanceService.toggleAttendance(memberId, gymId);

        return ResponseEntity.ok(ApiResponse.success(null, message));
    }

    // 2. SEARCH: For the "Manual Entry" Dialog
    @GetMapping("/search-member")
    public ResponseEntity<ApiResponse<List<Member>>> searchMemberForManualEntry(
            @RequestParam Long gymId,
            @RequestParam String query) {
        // This calls the new search method in MemberRepository via MemberService
        return ResponseEntity.ok(ApiResponse.success(memberService.searchMembers(gymId, query), "Members found"));
    }

    // 3. LIVE FEED: The main table data
    @GetMapping("/live/{gymId}")
    public ResponseEntity<ApiResponse<List<Attendance>>> getLiveFeed(@PathVariable Long gymId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getRecentAttendance(gymId), "Live feed fetched"));
    }

    // 4. STATS: For the "Currently Inside" Summary Card
    @GetMapping("/stats/{gymId}")
    public ResponseEntity<ApiResponse<Long>> getActiveStats(@PathVariable Long gymId) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getActiveCount(gymId), "Active count fetched"));
    }
}