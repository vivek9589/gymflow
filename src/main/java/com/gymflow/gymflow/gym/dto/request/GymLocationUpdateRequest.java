package com.gymflow.gymflow.gym.dto.request;


import lombok.Data;

@Data
public class GymLocationUpdateRequest {
    private Double latitude;
    private Double longitude;
}