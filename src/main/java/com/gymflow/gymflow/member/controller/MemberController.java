package com.gymflow.gymflow.member.controller;

import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.member.dto.request.MemberJoinRequest;
import com.gymflow.gymflow.member.dto.request.MemberUpdateRequest;
import com.gymflow.gymflow.member.dto.response.MemberResponse;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing gym members.
 * Provides endpoints for registration, updates, deletion, and queries.
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    /**
     * Public endpoint for self-registration via QR Code.
     */
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<MemberResponse>> joinGym(@Valid @RequestBody MemberJoinRequest request) {
        log.info("New member registration request for gymCode: {}", request.getGymCode());
        Member member = memberService.registerMember(request);
        MemberResponse response = memberService.updateMember(member.getId(),
                MemberUpdateRequest.builder()
                        .name(member.getName())
                        .phone(member.getPhone())
                        .email(member.getEmail())
                        .status(member.getStatus())
                        .build());
        return ResponseEntity.ok(ApiResponse.success(response, "Welcome! Registration successful."));
    }

    /**
     * Protected endpoint: Fetch all members of a gym.
     * Accessible only to OWNER role.
     */
    @GetMapping("/gym/{gymId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<Member>>> getGymMembers(@PathVariable Long gymId) {
        log.info("Fetching members for gymId: {}", gymId);
        return ResponseEntity.ok(ApiResponse.success(memberService.getAllMembersByGym(gymId), "Members fetched"));
    }

    /**
     * Protected endpoint: Delete a member.
     * Accessible only to OWNER role.
     */
    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long memberId) {
        log.info("Deleting member with id: {}", memberId);
        memberService.deleteMember(memberId);
        return ResponseEntity.ok(ApiResponse.success(null, "Member deleted successfully"));
    }

    /**
     * Protected endpoint: Update member details.
     * Accessible only to OWNER role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberUpdateRequest request) {
        log.info("Updating member with id: {}", id);
        MemberResponse updatedMember = memberService.updateMember(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedMember, "Member profile updated successfully."));
    }

    /**
     * Protected endpoint: Fetch member details by ID.
     * Accessible only to OWNER role.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberById(@PathVariable Long id) {
        log.info("Fetching member by id: {}", id);
        Member member = memberService.getMemberById(id);
        MemberResponse response = MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .phone(member.getPhone())
                .email(member.getEmail())
                .status(member.getStatus())
                .planName(member.getCurrentPlan() != null ? member.getCurrentPlan().getName() : "No Active Plan")
                .registrationDate(member.getRegistrationDate())
                .expiryDate(member.getExpiryDate())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response, "Member fetched successfully"));
    }
}