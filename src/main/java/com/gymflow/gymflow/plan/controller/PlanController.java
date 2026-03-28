package com.gymflow.gymflow.plan.controller;

import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.plan.dto.PlanDTO;
import com.gymflow.gymflow.plan.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing subscription plans.
 * Owners can create, update, delete plans.
 * Members can view plans during registration.
 */
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Slf4j
public class PlanController {

    private final PlanService planService;

    /**
     * Create a new plan for a gym.
     * Accessible only to gym owners.
     */
    @PostMapping("/gym/{gymId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<PlanDTO>> createPlan(
            @PathVariable Long gymId,
            @Valid @RequestBody PlanDTO dto) {
        log.info("API call: Create plan for gymId={}", gymId);
        return ResponseEntity.ok(ApiResponse.success(planService.createPlan(gymId, dto), "Plan created"));
    }

    /**
     * Fetch all active plans for a gym.
     * Accessible to members and owners.
     */
    @GetMapping("/gym/{gymId}")
    public ResponseEntity<ApiResponse<List<PlanDTO>>> getPlans(@PathVariable Long gymId) {
        List<PlanDTO> plans = planService.getPlansByGymId(gymId);
        return ResponseEntity.ok(ApiResponse.success(plans, "Plans retrieved successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PlanDTO>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> statusUpdate) {

        Boolean isActive = statusUpdate.get("active");
        if (isActive == null) {
            log.warn("Status update requested for ID: {} without 'active' field", id);
            return ResponseEntity.badRequest().body(ApiResponse.error("Field 'active' is required"));
        }

        PlanDTO updatedPlan = planService.updatePlanStatus(id, isActive);
        return ResponseEntity.ok(ApiResponse.success(updatedPlan, "Plan status updated successfully"));
    }

    /**
     * Fetch details of a single plan by ID.
     */
    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<PlanDTO>> getPlanById(@PathVariable Long planId) {
        log.info("API call: Fetch plan details for planId={}", planId);
        return ResponseEntity.ok(ApiResponse.success(planService.getPlanById(planId), "Plan details fetched"));
    }

    /**
     * Update an existing plan.
     * Accessible only to gym owners.
     */
    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<PlanDTO>> updatePlan(
            @PathVariable Long planId,
            @Valid @RequestBody PlanDTO dto) {
        log.info("API call: Update planId={}", planId);
        return ResponseEntity.ok(ApiResponse.success(planService.updatePlan(planId, dto), "Plan updated successfully"));
    }

    /**
     * Soft delete a plan (mark inactive).
     * Accessible only to gym owners.
     */
    @DeleteMapping("/{planId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable Long planId) {
        log.info("API call: Delete planId={}", planId);
        planService.deletePlan(planId);
        return ResponseEntity.ok(ApiResponse.success(null, "Plan marked inactive successfully"));
    }

    /**
     * Fetch all plans (active + inactive) for reporting.
     * Accessible only to gym owners.
     */
    @GetMapping("/report/{gymId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<PlanDTO>>> getPlanReport(@PathVariable Long gymId) {
        log.info("API call: Fetch plan report for gymId={}", gymId);
        return ResponseEntity.ok(ApiResponse.success(planService.getPlanReport(gymId), "Plan report fetched"));
    }
}
