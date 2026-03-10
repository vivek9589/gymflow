package com.gymflow.gymflow.notification.scheduler;

import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.repository.NotificationTemplateRepository;
import com.gymflow.gymflow.notification.service.NotificationEventService;
import com.gymflow.gymflow.notification.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Component
public class ExpiryReminderScheduler {

    private final MemberRepository memberRepository;
    private final NotificationEventService notificationEventService;
    private final NotificationTemplateRepository templateRepository;

    public ExpiryReminderScheduler(MemberRepository memberRepository,
                                   NotificationEventService notificationEventService,
                                   NotificationTemplateRepository templateRepository) {
        this.memberRepository = memberRepository;
        this.notificationEventService = notificationEventService;
        this.templateRepository = templateRepository;
    }

    @Scheduled(cron = "0 0 2 * * ?") // every day at 2 AM
    public void sendExpiryReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(3);

        List<Member> members = memberRepository.findByExpiryDate(targetDate);

        Optional<NotificationTemplate> templateOpt = templateRepository.findByName("EXPIRY_REMINDER");

        if (templateOpt.isEmpty()) {
            System.err.println("Expiry reminder template not found!");
            return;
        }

        NotificationTemplate template = templateOpt.get();

        for (Member member : members) {
            try {
                notificationEventService.createNotification(member, template);
                System.out.printf("Created expiry reminder event for %s (%s)%n",
                        member.getName(), member.getPhone());
            } catch (Exception e) {
                System.err.printf("Failed to create reminder for %s (%s): %s%n",
                        member.getName(), member.getPhone(), e.getMessage());
            }
        }
    }
}