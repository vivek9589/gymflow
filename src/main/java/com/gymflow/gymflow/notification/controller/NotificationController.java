package com.gymflow.gymflow.notification.controller;

import com.gymflow.gymflow.common.exception.ResourceNotFoundException;
import com.gymflow.gymflow.notification.dto.request.NotificationRequest;
import com.gymflow.gymflow.notification.entity.NotificationEvent;
import com.gymflow.gymflow.notification.repository.NotificationEventRepository;
import com.gymflow.gymflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.gymflow.gymflow.notification.dto.request.PromotionRequest;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationEventRepository eventRepository;

    // Send to Specific Member
    @PostMapping("/send")
    public ResponseEntity<String> send(@RequestBody NotificationRequest request) {
        notificationService.sendNotification(request.getMemberId(), request.getTemplateId());
        return ResponseEntity.ok("Notification triggered successfully");
    }

    // Send to All Members (Promotion/Holiday)
    @PostMapping("/promotion/all")
    public ResponseEntity<String> sendToAll(@RequestBody PromotionRequest request) {
        int count = notificationService.sendToAll(request.getTemplateId());
        return ResponseEntity.ok("Successfully processed notifications for " + count + " members.");
    }

    // Get Log by ID
    @GetMapping("/{id}/status")
    public ResponseEntity<NotificationEvent> getStatus(@PathVariable Long id) {
        NotificationEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return ResponseEntity.ok(event);
    }

    // Get All Logs
    @GetMapping("/logs")
    public ResponseEntity<List<NotificationEvent>> getLogs() {
        return ResponseEntity.ok(eventRepository.findAll());
    }
}