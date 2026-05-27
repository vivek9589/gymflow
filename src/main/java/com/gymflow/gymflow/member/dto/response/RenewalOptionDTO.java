package com.gymflow.gymflow.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenewalOptionDTO {
    private Long planId;
    private String name;
    private BigDecimal price;
    private Integer durationInDays;
}
