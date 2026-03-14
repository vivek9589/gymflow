package com.gymflow.gymflow.dashboard.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceHealthDTO {
    private int activeThisWeek;
    private int inactiveThisWeek;

    public AttendanceHealthDTO(int active, int inactive) {
        this.activeThisWeek = active;
        this.inactiveThisWeek = inactive;
    }
    // Getters and Setters
}