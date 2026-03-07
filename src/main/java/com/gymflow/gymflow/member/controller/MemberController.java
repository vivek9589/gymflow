package com.gymflow.gymflow.member.controller;


import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.member.dto.request.MemberJoinRequest;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // PUBLIC: For self-registration via QR Code
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<Member>> joinGym(@Valid @RequestBody MemberJoinRequest request) {
        Member member = memberService.registerMember(request);
        return ResponseEntity.ok(ApiResponse.success(member, "Welcome! Registration successful."));
    }

    // PROTECTED: For Owner Dashboard
    @GetMapping("/gym/{gymId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<Member>>> getGymMembers(@PathVariable Long gymId) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getAllMembersByGym(gymId), "Members fetched"));
    }


    // NEW: Delete Member API
    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok(ApiResponse.success(null, "Member deleted successfully"));
    }
}