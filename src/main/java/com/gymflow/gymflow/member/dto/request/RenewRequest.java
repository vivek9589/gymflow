package com.gymflow.gymflow.member.dto.request;

import lombok.Data;

@Data
public class RenewRequest {

    private Long memberId;
    private Long planId;

    private Double amountPaid;
    private String paymentMode;
    private String transactionRef;
}
