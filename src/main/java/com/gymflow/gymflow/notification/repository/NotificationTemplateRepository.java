package com.gymflow.gymflow.notification.repository;

import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.enums.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByName(String name);
    List<NotificationTemplate> findByChannelType(ChannelType type);
}