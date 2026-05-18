package com.gymflow.gymflow.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;
    private Long subscriptionId;
    private Long gymId;

    private BigDecimal amount;
    private String paymentMode; // CASH, UPI, ONLINE_GATEWAY
    private String status; // SUCCESS, PENDING, FAILED

    private String transactionRef; // Manual reference ID or online gateway tracking token

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}