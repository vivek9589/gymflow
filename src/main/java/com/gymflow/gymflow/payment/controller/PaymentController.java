package com.gymflow.gymflow.payment.controller;

import com.gymflow.gymflow.common.dto.ApiResponse;
import com.gymflow.gymflow.payment.dto.request.AddPaymentRequest;
import com.gymflow.gymflow.payment.entity.Payment;
import com.gymflow.gymflow.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Payment>> addPayment(@RequestBody AddPaymentRequest req) {

        Payment payment = paymentService.addPayment(
                req.getMemberId(),
                req.getSubscriptionId(),
                req.getGymId(),
                req.getAmount(),
                req.getPaymentMode(),
                req.getTransactionRef()
        );

        return ResponseEntity.ok(ApiResponse.success(payment, "Payment added"));
    }
}