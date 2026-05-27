package com.gymflow.gymflow.payment.service.impl;


import com.gymflow.gymflow.member.dto.response.PaymentHistoryDTO;
import com.gymflow.gymflow.payment.entity.Payment;
import com.gymflow.gymflow.payment.entity.Subscription;
import com.gymflow.gymflow.payment.repository.PaymentRepository;
import com.gymflow.gymflow.payment.repository.SubscriptionRepository;
import com.gymflow.gymflow.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    @Override
    @Transactional
    public Payment addPayment(Long memberId, Long subscriptionId, Long gymId,
                              BigDecimal amount, String mode, String ref) {
        log.info("Recording upfront full payment for subscriptionId={}", subscriptionId);

        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription target record not found"));

        Payment payment = Payment.builder()
                .memberId(memberId)
                .subscriptionId(subscriptionId)
                .gymId(gymId)
                .amount(amount)
                .paymentMode(mode != null ? mode : "CASH")
                .status("SUCCESS")
                .transactionRef(ref)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Instantly transition the linked subscription container to ACTIVE state
        sub.setStatus("ACTIVE");
        subscriptionRepository.save(sub);

        log.info("Payment saved successfully. Subscription {} is now fully ACTIVE", subscriptionId);
        return savedPayment;
    }


    @Override
    public List<Payment> getPaymentsForMember(Long memberId) {
        log.info("Fetching payment history for memberId={}", memberId);
        return paymentRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }
}