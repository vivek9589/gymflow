package com.gymflow.gymflow.payment.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddPaymentRequest {
    private Long subscriptionId;
    private Long memberId;
    private Long gymId;
    private BigDecimal amount;
    private String paymentMode;
    private String transactionRef;
}