package com.gymflow.gymflow.notification.service;



public interface NotificationService {
    void sendNotification(Long memberId, Long templateId);
    int sendToAll(Long templateId);
}