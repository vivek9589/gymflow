package com.gymflow.gymflow.notification.controller;

import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.notification.dto.request.PromotionRequest;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.repository.NotificationTemplateRepository;
import com.gymflow.gymflow.notification.service.NotificationEventService;
import com.gymflow.gymflow.notification.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/notifications")
public class PromotionController {

    private final MemberRepository memberRepository;
    private final NotificationEventService notificationEventService;
    private final NotificationTemplateRepository templateRepository;

    // Send promotion to ALL members
    @PostMapping("/promotion/all")
    public ResponseEntity<String> sendPromotionToAll(@RequestBody PromotionRequest request) {
        List<Member> members = memberRepository.findAll();
        if (members.isEmpty()) {
            return ResponseEntity.badRequest().body("No members found!");
        }

        Optional<NotificationTemplate> templateOpt = templateRepository.findById(request.getTemplateId());
        if (templateOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Promotion template not found!");
        }

        NotificationTemplate template = templateOpt.get();
        int count = 0;

        for (Member member : members) {
            try {
                notificationEventService.createNotification(member, template);
                count++;
            } catch (Exception e) {
                // log error but continue
                System.err.println("Failed to send promotion to " + member.getName() + ": " + e.getMessage());
            }
        }

        return ResponseEntity.ok("Promotion sent to " + count + " members.");
    }

    // Send promotion to a SPECIFIC member
    @PostMapping("/promotion/{memberId}")
    public ResponseEntity<String> sendPromotionToMember(@PathVariable Long memberId) {
        Optional<Member> memberOpt = memberRepository.findById(memberId);
        Optional<NotificationTemplate> templateOpt = templateRepository.findById(3L); // use DB template ID

        if (memberOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Member not found!");
        }
        if (templateOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Promotion template not found!");
        }

        Member member = memberOpt.get();
        NotificationTemplate template = templateOpt.get();

        try {
            //notificationService.sendNotification(member, template);
            notificationEventService.createNotification(member,template);
            return ResponseEntity.ok("Promotion sent to " + member.getName() + " (" + member.getPhone() + ")");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to send promotion: " + e.getMessage());
        }
    }
}