package com.gymflow.gymflow.payment.service;

import com.gymflow.gymflow.member.dto.response.PaymentHistoryDTO;
import com.gymflow.gymflow.payment.entity.Payment;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    Payment addPayment(Long memberId, Long subscriptionId, Long gymId,
                       BigDecimal amount, String mode, String ref);


    // Return raw Payment entities (not DTOs) to keep service layer consistent
    List<Payment> getPaymentsForMember(Long memberId);
}