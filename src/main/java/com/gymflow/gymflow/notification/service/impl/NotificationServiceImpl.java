package com.gymflow.gymflow.notification.service.impl;

import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.sender.NotificationSender;
import com.gymflow.gymflow.notification.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final List<NotificationSender> senders;

    public NotificationServiceImpl(List<NotificationSender> senders) {
        this.senders = senders;
    }

    @Override
    public void sendNotification(Member member, NotificationTemplate template) {
        String message = renderTemplate(member, template);

        senders.stream()
                .filter(sender -> sender.getChannelName().equalsIgnoreCase(template.getChannel().getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported channel: " + template.getChannel().getName()))
                .send(member.getPhone(), message);
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