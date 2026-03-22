package com.gymflow.gymflow.notification.sender.impl;


import com.gymflow.gymflow.common.exception.NotificationSendException;
import com.gymflow.gymflow.notification.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Primary
public class EvolutionWhatsAppSender implements NotificationSender {

    private final RestTemplate restTemplate;

    @Value("${evolution.api-url}")
    private String evolutionApiUrl;

    @Value("${evolution.instance-name}")
    private String instanceName;

    @Value("${evolution.api-key:}")
    private String apiKey;

    public EvolutionWhatsAppSender(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getChannelName() {
        return "WHATSAPP";
    }

    @Override
    public void send(String phone, String message) {

        String url = evolutionApiUrl + "/message/sendText/" + instanceName;

        Map<String, Object> body = new HashMap<>();
        body.put("number", phone);
        body.put("text", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("apikey", apiKey);
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            log.info("WhatsApp sent to {} via Evolution API. Response: {}", phone, response.getBody());

        } catch (Exception e) {

            log.error("Failed to send WhatsApp to {}: {}", phone, e.getMessage(), e);

            throw new NotificationSendException("Evolution API send failed: " + e.getMessage());
        }
    }
}