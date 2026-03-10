package com.gymflow.gymflow.notification.repository;

import com.gymflow.gymflow.notification.entity.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {
}