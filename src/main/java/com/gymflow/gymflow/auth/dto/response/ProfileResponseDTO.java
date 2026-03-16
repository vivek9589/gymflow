package com.gymflow.gymflow.auth.dto.response;

import com.gymflow.gymflow.gym.dto.response.GymResponseDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProfileResponseDTO {
    private Long id;
    private String ownerName;
    private String email;
    private String role;
    private GymResponseDTO gym;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
