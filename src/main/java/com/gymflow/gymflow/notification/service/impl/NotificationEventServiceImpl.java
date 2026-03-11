package com.gymflow.gymflow.notification.service.impl;

import com.gymflow.gymflow.common.exception.NotificationSendException;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.notification.entity.NotificationEvent;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.enums.NotificationStatus;
import com.gymflow.gymflow.notification.repository.NotificationEventRepository;
import com.gymflow.gymflow.notification.sender.impl.N8nWebhookSender;
import com.gymflow.gymflow.notification.service.NotificationEventService;
import com.gymflow.gymflow.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
public class NotificationEventServiceImpl implements NotificationEventService {

    private final NotificationEventRepository eventRepository;
    private final NotificationService notificationService;

    public NotificationEventServiceImpl(NotificationEventRepository eventRepository,
                                        NotificationService notificationService) {
        this.eventRepository = eventRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void createNotification(Member member, NotificationTemplate template) {
        NotificationEvent event = new NotificationEvent();
        event.setMember(member);
        event.setTemplate(template);
        event.setChannel(template.getChannel());
        event.setStatus(NotificationStatus.PENDING);
        event.setCreatedAt(LocalDateTime.now());

        event = eventRepository.save(event);

        try {
            notificationService.sendNotification(member, template);
            event.setStatus(NotificationStatus.SENT);
            event.setProcessedAt(LocalDateTime.now());
            log.info("Notification event {} marked as SENT", event.getId());
        } catch (NotificationSendException e) {
            event.setStatus(NotificationStatus.FAILED);
            event.setErrorMessage(e.getMessage());
            log.error("Notification event {} FAILED: {}", event.getId(), e.getMessage());
        }

        eventRepository.save(event);
    }
}