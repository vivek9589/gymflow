package com.gymflow.gymflow.notification.sender.impl;


import com.gymflow.gymflow.common.exception.NotificationSendException;
import com.gymflow.gymflow.notification.enums.ChannelType;
import com.gymflow.gymflow.notification.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;



@Slf4j
@Component
public class N8nWebhookSender implements NotificationSender {

    private final RestTemplate restTemplate;
    private final String webhookUrl;

    public N8nWebhookSender(RestTemplate restTemplate,
                            @Value("${n8n.webhook-url}") String webhookUrl) {
        this.restTemplate = restTemplate;
        this.webhookUrl = webhookUrl;
    }

    @Override
    public String getChannelName() {
        return ChannelType.N8N.name();
    }

    @Override
    public void send(String phone, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("phone", phone);
        payload.put("message", message);

        try {
            String response = restTemplate.postForObject(webhookUrl, payload, String.class);
            log.info("n8n webhook sent to {}. Response: {}", phone, response);
        } catch (Exception e) {
            log.error("Failed to send to n8n webhook for {}: {}", phone, e.getMessage(), e);
            throw new NotificationSendException("n8n webhook send failed"+e);
        }
    }
}