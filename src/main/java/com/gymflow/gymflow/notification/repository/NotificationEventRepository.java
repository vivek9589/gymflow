package com.gymflow.gymflow.notification.repository;

import com.gymflow.gymflow.notification.entity.NotificationEvent;
import com.gymflow.gymflow.notification.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {

    List<NotificationEvent> findByGymIdAndStatus(Long gymId, NotificationStatus status);

    List<NotificationEvent> findByMemberId(Long memberId);

    List<NotificationEvent> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(NotificationStatus status);

    Optional<NotificationEvent> findTopByMemberIdOrderByCreatedAtDesc(Long memberId);

}