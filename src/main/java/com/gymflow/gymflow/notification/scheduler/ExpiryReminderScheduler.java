package com.gymflow.gymflow.notification.scheduler;

import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.repository.NotificationTemplateRepository;
import com.gymflow.gymflow.notification.service.NotificationEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Slf4j
@Component
public class ExpiryReminderScheduler {

    private final MemberRepository memberRepository;
    private final NotificationEventService notificationEventService;
    private final NotificationTemplateRepository templateRepository;

    @Value("${scheduler.expiry-reminder.cron:0 0 2 * * ?}")
    private String cronExpression;

    public ExpiryReminderScheduler(MemberRepository memberRepository,
                                   NotificationEventService notificationEventService,
                                   NotificationTemplateRepository templateRepository) {
        this.memberRepository = memberRepository;
        this.notificationEventService = notificationEventService;
        this.templateRepository = templateRepository;
    }

    /**
     * Scheduled job to send expiry reminders to members whose subscriptions
     * expire in 3 days. Runs daily at 2 AM by default.
     */
    @Scheduled(cron = "${scheduler.expiry-reminder.cron}")
    public void sendExpiryReminders() {
        log.info("Starting expiry reminder scheduler at {}", LocalDateTime.now());

        LocalDate targetDate = LocalDate.now().plusDays(3);
        List<Member> members = memberRepository.findByExpiryDate(targetDate);

        Optional<NotificationTemplate> templateOpt = templateRepository.findByName("EXPIRY_REMINDER");
        if (templateOpt.isEmpty()) {
            log.warn("Expiry reminder template not found. Skipping job.");
            return;
        }

        NotificationTemplate template = templateOpt.get();
        int successCount = 0;
        int failureCount = 0;

        for (Member member : members) {
            try {
                notificationEventService.createNotification(member, template);
                log.info("Created expiry reminder event for {} ({})", member.getName(), member.getPhone());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to create reminder for {} ({}): {}",
                        member.getName(), member.getPhone(), e.getMessage(), e);
                failureCount++;
            }
        }

        log.info("Expiry reminder scheduler completed. Success: {}, Failures: {}", successCount, failureCount);
    }
}