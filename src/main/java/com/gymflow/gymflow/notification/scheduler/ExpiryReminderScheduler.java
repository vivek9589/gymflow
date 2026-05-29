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
     * expire in 1 day (tomorrow). Runs daily at 2 AM.
     */
    @Scheduled(cron = "${scheduler.expiry-reminder.cron}")
    public void sendExpiryReminders() {
        log.info("Starting production expiry reminder scheduler execution at {}", LocalDateTime.now());

        // 🔥 FIXED: Adjusted target date boundary to look exactly 1 day ahead (Tomorrow)
        LocalDate targetDate = LocalDate.now().plusDays(1);

        // Fetch active, non-deleted members expiring tomorrow
        List<Member> members = memberRepository.findByExpiryDateAndDeletedFalse(targetDate);

        if (members.isEmpty()) {
            log.info("No members found expiring tomorrow ({}). Scheduler lifecycle idle.", targetDate);
            return;
        }

        Optional<NotificationTemplate> templateOpt = templateRepository.findByName("EXPIRY_REMINDER");
        if (templateOpt.isEmpty()) {
            log.error("CRITICAL CONFIGURATION ERROR: Template 'EXPIRY_REMINDER' missing from database tables.");
            return;
        }

        NotificationTemplate template = templateOpt.get();
        int successCount = 0;
        int failureCount = 0;

        for (Member member : members) {
            // Respect member privacy / notification channel opt-out flags
            if (!member.isWhatsappEnabled()) {
                log.info("Skipping notification delivery routing for member: {} [Channel Opt-Out]", member.getName());
                continue;
            }

            try {
                notificationService.sendNotification(member.getId(), template.getId());
                log.info("Successfully fired expiry reminder event sequence for: {} ({})", member.getName(), member.getPhone());
                successCount++;
            } catch (Exception e) {
                log.error("Failed downstream delivery routing for member: {} ({}). Cause: {}",
                        member.getName(), member.getPhone(), e.getMessage());
                failureCount++;
            }
        }

        log.info("Expiry reminder scheduler run finalized. Dispatched successfully: {}, Total Failures: {}", successCount, failureCount);
    }
}