package com.gymflow.gymflow.notification.sender;

public interface NotificationSender {

    default void send(String instanceName, String phone, String message) {
        throw new UnsupportedOperationException("Instance-based send not supported");
    }

    void send(String phone, String message);

    String getChannelName();
}