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

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    @Override
    public Subscription createSubscription(Long memberId, Long planId, Long gymId, BigDecimal amountPaid) {

        log.info("Creating subscription for memberId={}", memberId);

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(plan.getDurationInDays());

        BigDecimal total = plan.getPrice();
        BigDecimal due = total.subtract(amountPaid);

        String status = due.compareTo(BigDecimal.ZERO) == 0 ? "ACTIVE" : "PARTIAL";

        Subscription subscription = Subscription.builder()
                .memberId(memberId)
                .planId(planId)
                .gymId(gymId)
                .startDate(startDate)
                .endDate(endDate)
                .totalAmount(total)
                .paidAmount(amountPaid)
                .dueAmount(due)
                .status(status)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);

        log.info("Subscription created with id={}", saved.getId());

        return saved;
    }
}
