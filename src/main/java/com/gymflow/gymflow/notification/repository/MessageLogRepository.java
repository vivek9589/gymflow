package com.gymflow.gymflow.notification.repository;

import com.gymflow.gymflow.notification.entity.MessageLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageLogRepository extends JpaRepository<MessageLog, Long> {
}