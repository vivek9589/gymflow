package com.gymflow.gymflow.gym.controller;



import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.gym.dto.GymDTO;
import com.gymflow.gymflow.gym.service.GymService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gyms")
@RequiredArgsConstructor
public class GymController {

    private final GymService gymService;

    // Public endpoint for members to see gym details before joining
    @GetMapping("/public/{code}")
    public ResponseEntity<ApiResponse<GymDTO>> getPublicGymDetails(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(gymService.getGymByCode(code), "Gym details fetched"));
    }

    // Protected: Only the Owner can update their gym
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<GymDTO>> updateGym(@PathVariable Long id, @RequestBody GymDTO dto) {
        return ResponseEntity.ok(ApiResponse.success(gymService.updateGym(id, dto), "Gym updated successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'STAFF')")
    public ResponseEntity<ApiResponse<GymDTO>> getGymDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(gymService.getGymById(id), "Gym details fetched"));
    }
}