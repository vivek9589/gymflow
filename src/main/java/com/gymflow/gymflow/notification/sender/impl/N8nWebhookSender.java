package com.gymflow.gymflow.notification.sender.impl;


import com.gymflow.gymflow.notification.sender.NotificationSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

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
        return "N8N"; // or "WEBHOOK"
    }

    @Override
    public void send(String phone, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("phone", phone);
        payload.put("message", message);

        try {
            String response = restTemplate.postForObject(webhookUrl, payload, String.class);
            System.out.println("n8n response: " + response);
        } catch (Exception e) {
            System.err.println("Failed to send to n8n webhook: " + e.getMessage());
        }
    }
}