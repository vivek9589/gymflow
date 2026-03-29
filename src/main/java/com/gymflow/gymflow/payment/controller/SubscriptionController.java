package com.gymflow.gymflow.payment.controller;

import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.payment.dto.request.CreateSubscriptionRequest;
import com.gymflow.gymflow.payment.entity.Subscription;
import com.gymflow.gymflow.payment.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<ApiResponse<Subscription>> create(@RequestBody CreateSubscriptionRequest req) {

        Subscription sub = subscriptionService.createSubscription(
                req.getMemberId(),
                req.getPlanId(),
                req.getGymId(),
                req.getAmountPaid()
        );

        return ResponseEntity.ok(ApiResponse.success(sub, "Subscription created"));
    }
}
