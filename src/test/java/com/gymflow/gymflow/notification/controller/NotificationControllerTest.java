package com.gymflow.gymflow.notification.controller;

import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.notification.entity.NotificationEvent;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.enums.NotificationStatus;
import com.gymflow.gymflow.notification.service.NotificationEventService;
import com.gymflow.gymflow.notification.repository.NotificationEventRepository;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.notification.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationEventService eventService;

    @MockBean
    private NotificationEventRepository eventRepository;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private NotificationTemplateRepository templateRepository;

    @Test
    void testSendNotification_success() throws Exception {
        Member member = new Member();
        member.setId(1L);
        member.setName("John Doe");
        member.setPhone("1234567890");

        NotificationTemplate template = new NotificationTemplate();
        template.setId(1L);
        template.setName("EXPIRY_REMINDER");

        NotificationEvent event = new NotificationEvent();
        event.setId(100L);
        event.setStatus(NotificationStatus.SENT);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(eventRepository.findTopByMemberIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(event));

        mockMvc.perform(post("/api/notifications/send")
                        .contentType("application/json")
                        .content("{\"memberId\":1,\"templateId\":1,\"channel\":\"WHATSAPP\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(100))
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    void testGetStatus_success() throws Exception {
        NotificationEvent event = new NotificationEvent();
        event.setId(200L);
        event.setStatus(NotificationStatus.SENT);

        when(eventRepository.findById(200L)).thenReturn(Optional.of(event));

        mockMvc.perform(get("/api/notifications/200/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(200))
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    void testGetLogs_success() throws Exception {
        NotificationEvent event = new NotificationEvent();
        event.setId(300L);
        event.setStatus(NotificationStatus.SENT);

        when(eventRepository.findAll()).thenReturn(java.util.List.of(event));

        mockMvc.perform(get("/api/notifications/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(300))
                .andExpect(jsonPath("$[0].status").value("SENT"));
    }
}