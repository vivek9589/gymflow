package com.gymflow.gymflow.payment.service.impl;


import com.gymflow.gymflow.payment.entity.Payment;
import com.gymflow.gymflow.payment.entity.Subscription;
import com.gymflow.gymflow.payment.repository.PaymentRepository;
import com.gymflow.gymflow.payment.repository.SubscriptionRepository;
import com.gymflow.gymflow.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    public Payment addPayment(Long memberId, Long subscriptionId, Long gymId,
                              BigDecimal amount, String mode, String ref) {

        log.info("Adding payment for subscriptionId={}", subscriptionId);

        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        Payment payment = Payment.builder()
                .memberId(memberId)
                .subscriptionId(subscriptionId)
                .gymId(gymId)
                .amount(amount)
                .paymentMode(mode)
                .status("SUCCESS")
                .transactionRef(ref)
                .build();

        paymentRepository.save(payment);

        // update subscription
        BigDecimal newPaid = sub.getPaidAmount().add(amount);
        BigDecimal newDue = sub.getTotalAmount().subtract(newPaid);

        sub.setPaidAmount(newPaid);
        sub.setDueAmount(newDue);

        if (newDue.compareTo(BigDecimal.ZERO) == 0) {
            sub.setStatus("ACTIVE");
        } else {
            sub.setStatus("PARTIAL");
        }

        subscriptionRepository.save(sub);

        log.info("Payment added successfully for subscriptionId={}", subscriptionId);

        return payment;
    }
}