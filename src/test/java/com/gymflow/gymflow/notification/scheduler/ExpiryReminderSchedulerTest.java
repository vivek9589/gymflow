package com.gymflow.gymflow.notification.scheduler;

import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.repository.NotificationTemplateRepository;
import com.gymflow.gymflow.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class ExpiryReminderSchedulerTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @InjectMocks
    private ExpiryReminderScheduler scheduler;

    public ExpiryReminderSchedulerTest() {
        MockitoAnnotations.openMocks(this); // initialize mocks
    }

    @Test
    void testSendExpiryReminders_success() {
        // Arrange
        Member member = new Member();
        member.setName("John Doe");
        member.setPhone("1234567890");
        member.setExpiryDate(LocalDate.now().plusDays(3));

        NotificationTemplate template = new NotificationTemplate();
        template.setName("EXPIRY_REMINDER");
        template.setTemplateBody("Hello {{name}}, your membership expires on {{expiry_date}}.");

        when(memberRepository.findByExpiryDate(any(LocalDate.class)))
                .thenReturn(List.of(member));
        when(templateRepository.findByName("EXPIRY_REMINDER"))
                .thenReturn(Optional.of(template));

        // Act
        scheduler.sendExpiryReminders();

        // Assert
        verify(notificationService, times(1))
                .sendNotification(member.getId(), template.getId());
    }

    @Test
    void testSendExpiryReminders_templateMissing() {
        when(templateRepository.findByName("EXPIRY_REMINDER"))
                .thenReturn(Optional.empty());

        scheduler.sendExpiryReminders();

        verifyNoInteractions(notificationService);
    }
}