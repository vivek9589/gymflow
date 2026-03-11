package com.gymflow.gymflow.notification.controller;

import com.gymflow.gymflow.common.exception.ResourceNotFoundException;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.notification.dto.request.NotificationRequest;
import com.gymflow.gymflow.notification.dto.response.NotificationResponse;
import com.gymflow.gymflow.notification.entity.NotificationEvent;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.repository.NotificationEventRepository;
import com.gymflow.gymflow.notification.repository.NotificationTemplateRepository;
import com.gymflow.gymflow.notification.service.NotificationEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationEventService eventService;
    private final NotificationEventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final NotificationTemplateRepository templateRepository;

    public NotificationController(NotificationEventService eventService,
                                  NotificationEventRepository eventRepository,
                                  MemberRepository memberRepository,
                                  NotificationTemplateRepository templateRepository) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.memberRepository = memberRepository;
        this.templateRepository = templateRepository;
    }


    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(@RequestBody NotificationRequest request) {
        log.info("Received send notification request for member {} with template {}",
                request.getMemberId(), request.getTemplateId());

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        NotificationTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        eventService.createNotification(member, template);

        Optional<NotificationEvent> event =
                eventRepository.findTopByMemberIdOrderByCreatedAtDesc(member.getId());

        return ResponseEntity.ok(new NotificationResponse(event.get().getId(), event.get().getStatus(), "Notification processed"));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<NotificationResponse> getStatus(@PathVariable Long id) {
        NotificationEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        return ResponseEntity.ok(new NotificationResponse(event.getId(), event.getStatus(), event.getErrorMessage()));
    }

    @GetMapping("/logs")
    public ResponseEntity<List<NotificationEvent>> getLogs() {
        List<NotificationEvent> events = eventRepository.findAll();
        return ResponseEntity.ok(events);
    }
}