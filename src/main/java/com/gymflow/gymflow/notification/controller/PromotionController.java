package com.gymflow.gymflow.notification.controller;

import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
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
    private final NotificationService notificationService;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationEventService notificationEventService;



    // Send promotion to ALL members
    @PostMapping("/promotion/all")
    public ResponseEntity<String> sendPromotionToAll(@RequestBody String message) {
        List<Member> members = memberRepository.findAll();
        int count = 0;

        Optional<NotificationTemplate> templateOpt = templateRepository.findById(3L); // use DB template ID

        if (templateOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Promotion template not found!");
        }
        NotificationTemplate template = templateOpt.get();


        for (Member member : members) {
            // Create a promotion notification for each member

            notificationEventService.createNotification(member, template);
            count++;
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