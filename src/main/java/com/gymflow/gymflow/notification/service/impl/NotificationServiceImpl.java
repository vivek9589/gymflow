package com.gymflow.gymflow.notification.service.impl;

import com.gymflow.gymflow.common.exception.NotificationSendException;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.sender.NotificationSender;
import com.gymflow.gymflow.notification.service.NotificationService;
import com.gymflow.gymflow.notification.utility.TemplateRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final Map<String, NotificationSender> senderRegistry;

    public NotificationServiceImpl(List<NotificationSender> senders) {
        this.senderRegistry = senders.stream()
                .collect(Collectors.toMap(NotificationSender::getChannelName, s -> s));
    }

    @Override
    @Async
    public void sendNotification(Member member, NotificationTemplate template) {
        String message = TemplateRenderer.render(template, member);

        NotificationSender sender = senderRegistry.get(template.getChannel().getType().name());
        if (sender == null) {
            throw new NotificationSendException("Unsupported channel: " + template.getChannel().getType());
        }

        try {
            sender.send(member.getPhone(), message);
            log.info("Notification sent to {} via {}", member.getName(), template.getChannel().getType());
        } catch (Exception e) {
            log.error("Failed to send notification to {} via {}: {}",
                    member.getName(), template.getChannel().getType(), e.getMessage(), e);
            throw new NotificationSendException("Send failed");
        }
    }
}