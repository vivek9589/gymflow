package com.gymflow.gymflow.dashboard.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpiringSoonDTO {
    private int count;
    private double potentialRevenue;

    public ExpiringSoonDTO(int count, double potentialRevenue) {
        this.count = count;
        this.potentialRevenue = potentialRevenue;
    }
    // Getters and Setters
}