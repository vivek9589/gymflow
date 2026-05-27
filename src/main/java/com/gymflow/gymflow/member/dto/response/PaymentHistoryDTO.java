package com.gymflow.gymflow.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryDTO {
    private Long id;
    private BigDecimal amount;
    private String paymentMode;
    private String status;
    private String transactionRef;
    private LocalDateTime createdAt;
}