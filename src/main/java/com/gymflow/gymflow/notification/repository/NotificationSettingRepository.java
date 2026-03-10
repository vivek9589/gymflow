package com.gymflow.gymflow.notification.repository;

import com.gymflow.gymflow.notification.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
}