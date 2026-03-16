package com.gymflow.gymflow.notification.scheduler;

import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.repository.NotificationTemplateRepository;
import com.gymflow.gymflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ExpiryReminderScheduler {

    private final MemberRepository memberRepository;
    private final NotificationService notificationService;
    private final NotificationTemplateRepository templateRepository;

    @Value("${scheduler.expiry-reminder.cron:0 0 2 * * ?}")
    private String cronExpression;

    /**
     * Scheduled job to send expiry reminders to active members whose subscriptions
     * expire in 3 days. Runs daily at 2 AM by default.
     */
    @Scheduled(cron = "${scheduler.expiry-reminder.cron}")
    public void sendExpiryReminders() {
        log.info("Starting expiry reminder scheduler at {}", LocalDateTime.now());

        // We only want to notify members expiring in 3 days
        LocalDate targetDate = LocalDate.now().plusDays(3);

        // CRITICAL: Updated to use the method that ignores soft-deleted members
        List<Member> members = memberRepository.findByExpiryDateAndDeletedFalse(targetDate);

        if (members.isEmpty()) {
            log.info("No members expiring on {}. Job finished.", targetDate);
            return;
        }

        Optional<NotificationTemplate> templateOpt = templateRepository.findByName("EXPIRY_REMINDER");
        if (templateOpt.isEmpty()) {
            log.warn("Expiry reminder template 'EXPIRY_REMINDER' not found. Skipping job.");
            return;
        }

        NotificationTemplate template = templateOpt.get();
        int successCount = 0;
        int failureCount = 0;

        for (Member member : members) {
            // Safety check: Don't send if WhatsApp is disabled for this specific member
            if (!member.isWhatsappEnabled()) {
                log.info("Skipping notification for {} - WhatsApp disabled", member.getName());
                continue;
            }

            try {
                notificationService.sendNotification(member.getId(), template.getId());
                log.info("Created expiry reminder event for {} ({})", member.getName(), member.getPhone());
                successCount++;
            } catch (Exception e) {
                log.error("Failed to create reminder for {} ({}): {}",
                        member.getName(), member.getPhone(), e.getMessage());
                failureCount++;
            }
        }

        log.info("Expiry reminder scheduler completed. Success: {}, Failures: {}", successCount, failureCount);
    }
}