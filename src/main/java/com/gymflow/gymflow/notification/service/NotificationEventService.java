package com.gymflow.gymflow.notification.service;

import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;

public interface NotificationEventService {
    void createNotification(Member member, NotificationTemplate template);
}