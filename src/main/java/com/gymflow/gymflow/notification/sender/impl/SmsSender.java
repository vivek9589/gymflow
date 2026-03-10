package com.gymflow.gymflow.notification.sender.impl;


import com.gymflow.gymflow.notification.sender.NotificationSender;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SmsSender implements NotificationSender {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void send(String phone, String message) {
        String apiUrl = UriComponentsBuilder
                .fromHttpUrl("https://api.smsprovider.com/send")
                .queryParam("to", phone)
                .queryParam("message", message)
                .toUriString();

        try {
            String response = restTemplate.getForObject(apiUrl, String.class);
            // TODO: log response or save to MessageLog
        } catch (Exception e) {
            // TODO: log error properly
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }
}