package com.gymflow.gymflow.dashboard.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExpiringSoonDTO {
    private int count;
    private double potentialRevenue;
    private List<ExpiringMemberDTO> members; // NEW FIELD

    public ExpiringSoonDTO(int count, double potentialRevenue, List<ExpiringMemberDTO> members) {
        this.count = count;
        this.potentialRevenue = potentialRevenue;
        this.members = members;
    }
}