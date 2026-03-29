package com.gymflow.gymflow.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EvolutionService {

    @Value("${evolution.api-url}")
    private String baseUrl;

    @Value("${evolution.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void createInstance(String instanceName) {

        String url = baseUrl + "/instance/create";

        Map<String, Object> body = new HashMap<>();
        body.put("instanceName", instanceName);
        body.put("integration", "WHATSAPP-BAILEYS");

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        log.info("Creating WhatsApp instance: {}", instanceName);

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, request, String.class);

        log.info("Evolution response: {}", response.getBody()); // 🔥 debug
    }
}
