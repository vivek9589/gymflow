package com.gymflow.gymflow.notification.service.impl;

import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.notification.entity.NotificationEvent;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.repository.NotificationEventRepository;
import com.gymflow.gymflow.notification.sender.impl.N8nWebhookSender;
import com.gymflow.gymflow.notification.service.NotificationEventService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationEventServiceImpl implements NotificationEventService {

    private final NotificationEventRepository eventRepository;
    private final N8nWebhookSender webhookSender;

    public NotificationEventServiceImpl(NotificationEventRepository eventRepository,
                                        N8nWebhookSender webhookSender) {
        this.eventRepository = eventRepository;
        this.webhookSender = webhookSender;
    }

    @Override
    public void createNotification(Member member, NotificationTemplate template) {
        String message = renderTemplate(member, template);

        NotificationEvent event = new NotificationEvent();
        event.setMember(member);
        event.setTemplate(template);
       // event.setType(template.getName());
      //  event.setMessage(message);
      //  event.setChannel(template.getChannel().getName());
        event.setStatus("PENDING");
        event.setCreatedAt(LocalDateTime.now());

        event = eventRepository.save(event);

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", event.getId());
        payload.put("type", template.getName());
        payload.put("memberName", member.getName());
        payload.put("phone", member.getPhone());
        payload.put("message", message);
        payload.put("channel", template.getChannel().getName());

        boolean success;
        try {
            webhookSender.send(member.getPhone(), message);
            success = true;
        } catch (Exception e) {
            success = false;
        }

        event.setStatus(success ? "SENT" : "FAILED");
        eventRepository.save(event);
    }

    private String renderTemplate(Member member, NotificationTemplate template) {
        String msg = template.getTemplateBody();
        msg = msg.replace("{{name}}", member.getName());
        msg = msg.replace("{{expiry_date}}",
                member.getExpiryDate() != null ? member.getExpiryDate().toString() : "");
        msg = msg.replace("{{gym_name}}", member.getGym().getName());
        return msg;
    }
}