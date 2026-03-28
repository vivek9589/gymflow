package com.gymflow.gymflow.notification.service.impl;

import com.gymflow.gymflow.common.exception.NotificationSendException;
import com.gymflow.gymflow.common.exception.ResourceNotFoundException;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.notification.entity.MessageLog;
import com.gymflow.gymflow.notification.entity.NotificationEvent;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.enums.NotificationStatus;
import com.gymflow.gymflow.notification.repository.MessageLogRepository;
import com.gymflow.gymflow.notification.repository.NotificationEventRepository;
import com.gymflow.gymflow.notification.repository.NotificationTemplateRepository;
import com.gymflow.gymflow.notification.sender.NotificationSender;
import com.gymflow.gymflow.notification.service.NotificationService;
import com.gymflow.gymflow.notification.utility.TemplateRenderer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationEventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationSender notificationSender; // Injected the Sender interface
    private final MessageLogRepository messageLogRepository;

    @Override
    @Transactional
    public void sendNotification(Long memberId, Long templateId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        processDispatch(member, template);
    }

    @Override
    @Transactional
    public int sendToAll(Long templateId) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        List<Member> members = memberRepository.findAll();
        int successCount = 0;

        for (Member member : members) {
            try {
                processDispatch(member, template);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send bulk notification to member {}: {}", member.getId(), e.getMessage());
            }
        }
        return successCount;
    }

    private void processDispatch(Member member, NotificationTemplate template) {

        NotificationEvent event = new NotificationEvent();
        event.setMember(member);
        event.setTemplate(template);
        event.setChannel(template.getChannel());
        event.setStatus(NotificationStatus.PENDING);
        event.setCreatedAt(LocalDateTime.now());
        event = eventRepository.save(event);

        MessageLog logEntry = MessageLog.builder()
                .gymId(member.getGym().getId())
                .memberId(member.getId())
                .channel(template.getChannel().getType().name())
                .message(template.getTemplateBody())
                .status("PENDING")
                .sentAt(LocalDateTime.now())
                .build();

        logEntry = messageLogRepository.save(logEntry);

        try {
            String personalizedMessage = TemplateRenderer.render(template, member);

            // Normalize phone number to E.164 format (+91 for India)
            String normalizedPhone = normalizePhone(member.getPhone());

            notificationSender.send(normalizedPhone, personalizedMessage);

            event.setStatus(NotificationStatus.SENT);
            event.setProcessedAt(LocalDateTime.now());

            logEntry.setStatus("SENT");

        } catch (NotificationSendException e) {
            event.setStatus(NotificationStatus.FAILED);
            event.setErrorMessage(e.getMessage());

            logEntry.setStatus("FAILED");
            logEntry.setResponse(e.getMessage());

            log.error("Notification send failed for member {}: {}", member.getId(), e.getMessage());
            throw e;

        } finally {
            eventRepository.save(event);
            messageLogRepository.save(logEntry);
        }
    }

    /**
     * Utility method to normalize phone numbers into E.164 format.
     * For India, prepend +91 if not already present.
     */
    private String normalizePhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            throw new NotificationSendException("Phone number is missing");
        }
        String trimmed = rawPhone.trim();
        if (!trimmed.startsWith("+")) {
            // Default to India country code
            return "+91" + trimmed;
        }
        return trimmed;
    }
}