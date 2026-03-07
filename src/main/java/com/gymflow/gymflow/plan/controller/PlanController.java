package com.gymflow.gymflow.plan.controller;

import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.plan.dto.PlanDTO;
import com.gymflow.gymflow.plan.service.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    // PROTECTED: Owners create plans for their gym
    @PostMapping("/gym/{gymId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<PlanDTO>> createPlan(
            @PathVariable Long gymId,
            @Valid @RequestBody PlanDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(planService.createPlan(gymId, dto), "Plan created"));
    }

    // PUBLIC/PROTECTED: Members can see plans during registration
    @GetMapping("/gym/{gymId}")
    public ResponseEntity<ApiResponse<List<PlanDTO>>> getGymPlans(@PathVariable Long gymId) {
        return ResponseEntity.ok(ApiResponse.success(planService.getPlansByGym(gymId), "Plans fetched"));
    }

    // UPDATE: Owner updates an existing plan
    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<PlanDTO>> updatePlan(
            @PathVariable Long planId,
            @Valid @RequestBody PlanDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(planService.updatePlan(planId, dto), "Plan updated successfully"));
    }

    // DELETE: Soft delete a plan
    @DeleteMapping("/{planId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable Long planId) {
        planService.deletePlan(planId);
        return ResponseEntity.ok(ApiResponse.success(null, "Plan deleted successfully"));
    }
}