package com.gymflow.gymflow.payment.service;

import com.gymflow.gymflow.payment.entity.Payment;

import java.math.BigDecimal;

public interface PaymentService {

    Payment addPayment(Long memberId, Long subscriptionId, Long gymId,
                       BigDecimal amount, String mode, String ref);
}
