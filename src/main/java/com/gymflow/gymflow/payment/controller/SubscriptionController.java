package com.gymflow.gymflow.payment.controller;

import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.payment.dto.request.CreateSubscriptionRequest;
import com.gymflow.gymflow.payment.entity.Subscription;
import com.gymflow.gymflow.payment.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<ApiResponse<Subscription>> create(@Valid @RequestBody CreateSubscriptionRequest req) {
        log.info("REST request to create subscription for memberId: {}", req.getMemberId());

        // Determine binary payment status
        boolean isPaid = req.isPaid() ||
                (req.getAmountPaid() != null && req.getAmountPaid().compareTo(BigDecimal.ZERO) > 0);

        Subscription sub = subscriptionService.createSubscription(
                req.getMemberId(),
                req.getPlanId(),
                req.getGymId(),
                isPaid,           // Passed down to match our signature updates
                LocalDate.now()   // Setting standard start date context
        );

        return ResponseEntity.ok(ApiResponse.success(sub, "Subscription created successfully"));
    }
}