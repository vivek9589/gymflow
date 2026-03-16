package com.gymflow.gymflow.gym.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GymResponseDTO {
    private Long id;
    private String name;
    // private String gymCode;
    private String address;
    private String contactNumber;
    private String city;
    private String state;
    private String pincode;
    private String website;
    private String logoUrl;
    private String description;
    private Integer establishedYear;
}