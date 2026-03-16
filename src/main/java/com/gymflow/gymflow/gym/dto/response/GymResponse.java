package com.gymflow.gymflow.gym.dto.response;



import lombok.Data;

@Data
public class GymResponse {
    private Long id;
    private String name;
    // private String gymCode;
    private String address;
    private String contactNumber;
}