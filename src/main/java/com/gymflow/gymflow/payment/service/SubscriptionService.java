package com.gymflow.gymflow.payment.service;

import com.gymflow.gymflow.payment.entity.Subscription;
import java.time.LocalDate;

public interface SubscriptionService {
    Subscription createSubscription(Long memberId, Long planId, Long gymId, boolean isPaid, LocalDate startDate);
}