package com.gymflow.gymflow.member.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RenewRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Plan ID is required")
    private Long planId;

    // Changed from Double to BigDecimal for precise financial calculations
    private BigDecimal amountPaid;

    private String paymentMode; // CASH, UPI, ONLINE_GATEWAY
    private String transactionRef;
}