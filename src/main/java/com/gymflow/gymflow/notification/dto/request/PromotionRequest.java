package com.gymflow.gymflow.notification.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromotionRequest {
    private Long templateId;
    private String message;
}