package com.gymflow.gymflow.attendance.dto.request;


import lombok.Data;

@Data
public class SelfCheckInRequest {
    private String token;
    private double latitude;
    private double longitude;
}