package com.gymflow.gymflow.notification.service;


import org.springframework.scheduling.annotation.Async;

public interface EmailService {

    @Async
    void sendPasswordResetEmail(String toEmail, String token);
}