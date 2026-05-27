package com.gymflow.gymflow.payment.service.impl;



import com.gymflow.gymflow.payment.entity.Subscription;
import com.gymflow.gymflow.payment.repository.SubscriptionRepository;
import com.gymflow.gymflow.payment.service.SubscriptionService;
import com.gymflow.gymflow.plan.entity.Plan;
import com.gymflow.gymflow.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    @Override
    public Subscription createSubscription(Long memberId, Long planId, Long gymId, boolean isPaid, LocalDate startDate) {
        log.info("Creating flat subscription container for memberId={}", memberId);

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Selected membership plan not found"));

        LocalDate start = (startDate != null) ? startDate : LocalDate.now();
        LocalDate endDate = start.plusDays(plan.getDurationInDays());

        Subscription subscription = Subscription.builder()
                .memberId(memberId)
                .planId(planId)
                .gymId(gymId)
                .startDate(start)
                .endDate(endDate)
                .totalAmount(plan.getPrice())
                .status(isPaid ? "ACTIVE" : "PENDING")
                .build();

        return subscriptionRepository.save(subscription);
    }

    @Override
    public List<Subscription> getSubscriptionsForMember(Long memberId) {
        log.info("Fetching subscription history logs for memberId: {}", memberId);

        // Fetches all plan logs linked to the member id from the database
        return subscriptionRepository.findByMemberIdOrderByStartDateDesc(memberId);
    }
}
