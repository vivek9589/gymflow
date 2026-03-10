package com.gymflow.gymflow.notification.repository;

import com.gymflow.gymflow.notification.entity.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationEventRepository
        extends JpaRepository<NotificationEvent, Long> {
}