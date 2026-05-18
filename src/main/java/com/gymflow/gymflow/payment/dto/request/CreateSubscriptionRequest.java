package com.gymflow.gymflow.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateSubscriptionRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Plan ID is required")
    private Long planId;

    @NotNull(message = "Gym ID is required")
    private Long gymId;

    private BigDecimal amountPaid;

    private boolean paid; // Explicit flag that can be set by the frontend
}