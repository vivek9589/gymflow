package com.gymflow.gymflow.notification.repository;

import com.gymflow.gymflow.notification.entity.NotificationChannel;
import com.gymflow.gymflow.notification.enums.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {
    Optional<NotificationChannel> findByType(ChannelType type);
}