package com.gymflow.gymflow.dashboard.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PopularPlanDTO {
    private String name;
    private Long count;  // must be Long

    public PopularPlanDTO(String name, Long count) {
        this.name = name;
        this.count = count;
    }
}