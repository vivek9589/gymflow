package com.gymflow.gymflow.payment.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSubscriptionRequest {
    private Long memberId;
    private Long planId;
    private Long gymId;
    private BigDecimal amountPaid;
}