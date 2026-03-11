package com.gymflow.gymflow.notification.sender.impl;


import com.gymflow.gymflow.common.exception.NotificationSendException;
import com.gymflow.gymflow.notification.enums.ChannelType;
import com.gymflow.gymflow.notification.sender.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class WhatsappSender implements NotificationSender {

    private final RestTemplate restTemplate;
    private final String apiUrl;

    public WhatsappSender(RestTemplate restTemplate,
                          @Value("${twilio.whatsapp.api-url}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
    }

    @Override
    public void send(String phone, String message) {
        String requestUrl = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .queryParam("to", phone)
                .queryParam("message", message)
                .toUriString();

        try {
            String response = restTemplate.getForObject(requestUrl, String.class);
            log.info("WhatsApp message sent to {}. Response: {}", phone, response);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}: {}", phone, e.getMessage(), e);
            throw new NotificationSendException("WhatsApp send failed" + e);
        }
    }

    @Override
    public String getChannelName() {
        return ChannelType.WHATSAPP.name();
    }
}