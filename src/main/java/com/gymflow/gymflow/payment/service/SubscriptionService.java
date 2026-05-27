package com.gymflow.gymflow.payment.service;

import com.gymflow.gymflow.payment.entity.Subscription;
import java.time.LocalDate;
import java.util.List;

public interface SubscriptionService {
    Subscription createSubscription(Long memberId, Long planId, Long gymId, boolean isPaid, LocalDate startDate);

    List<Subscription> getSubscriptionsForMember(Long memberId);
}