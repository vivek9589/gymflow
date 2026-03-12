package com.gymflow.gymflow.gym.controller;

import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.gym.dto.request.GymRequest;
import com.gymflow.gymflow.gym.dto.response.GymResponse;
import com.gymflow.gymflow.gym.service.GymService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gyms")
@RequiredArgsConstructor
@Slf4j
public class GymController {

    private final GymService gymService;

    // Public endpoint for members to see gym details before joining
    @GetMapping("/public/{code}")
    public ResponseEntity<ApiResponse<GymResponse>> getPublicGymDetails(@PathVariable String code) {
        log.info("Public request for gym details by code: {}", code);
        return ResponseEntity.ok(ApiResponse.success(gymService.getGymByCode(code), "Gym details fetched"));
    }

    // Protected: Only the Owner can update their gym
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<GymResponse>> updateGym(
            @PathVariable Long id,
            @Valid @RequestBody GymRequest request) {
        log.info("Owner updating gym with ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(gymService.updateGym(id, request), "Gym updated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'STAFF')")
    public ResponseEntity<ApiResponse<GymResponse>> getGymDetails(@PathVariable Long id) {
        log.info("Fetching gym details by ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(gymService.getGymById(id), "Gym details fetched"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getAllGyms() {
        log.info("Admin fetching all gyms");
        return ResponseEntity.ok(ApiResponse.success(gymService.getAllGyms(), "All gyms fetched successfully"));
    }
}