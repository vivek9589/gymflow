package com.gymflow.gymflow.notification.sender;

public interface NotificationSender {
    void send(String phone, String message);
    String getChannelName(); // e.g., "SMS", "WHATSAPP"
}