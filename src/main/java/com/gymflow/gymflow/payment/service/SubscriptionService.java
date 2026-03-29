package com.gymflow.gymflow.payment.service;

import com.gymflow.gymflow.payment.entity.Subscription;

import java.math.BigDecimal;

public interface SubscriptionService {
    Subscription createSubscription(Long memberId, Long planId, Long gymId, BigDecimal amountPaid);
}
