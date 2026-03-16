package com.gymflow.gymflow.auth.dto.response;


import lombok.Data;

@Data
public class GymOwnerResponse {
    private Long id;
    private String ownerName;
    private String email;
    private String gymName;
    // private String gymCode;
}