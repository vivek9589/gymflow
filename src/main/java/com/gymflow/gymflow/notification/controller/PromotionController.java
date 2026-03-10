package com.gymflow.gymflow.notification.controller;

import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;
import com.gymflow.gymflow.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class PromotionController {

    private final NotificationService notificationService;
    private final MemberRepository memberRepository;

    public PromotionController(NotificationService notificationService,
                               MemberRepository memberRepository) {
        this.notificationService = notificationService;
        this.memberRepository = memberRepository;
    }

    @PostMapping("/promotion")
    public ResponseEntity<String> sendPromotion(@RequestBody String message) {
        List<Member> members = memberRepository.findAll();

        int count = 0;
        for (Member member : members) {
            // Here you could wrap message in a NotificationTemplate if needed
            notificationService.sendNotification(member,
                    new NotificationTemplate(null, "PROMOTION", message, null, null));
            count++;
        }

        return ResponseEntity.ok("Promotion sent to " + count + " members.");
    }
}