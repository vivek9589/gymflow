package com.gymflow.gymflow.notification.entity;

import com.gymflow.gymflow.gym.entity.Gym;
import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.notification.enums.NotificationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "notification_events")
@Getter @Setter @NoArgsConstructor
public class NotificationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name = "gym_id")
    private Gym gym;

    @ManyToOne @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne @JoinColumn(name = "template_id")
    private NotificationTemplate template;

    @ManyToOne @JoinColumn(name = "channel_id")
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    private String errorMessage; // capture failure reason
}