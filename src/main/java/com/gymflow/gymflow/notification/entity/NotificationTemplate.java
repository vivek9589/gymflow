package com.gymflow.gymflow.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "notification_templates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String templateBody;

    private LocalDateTime createdAt;

    @ManyToOne @JoinColumn(name = "channel_id")
    private NotificationChannel channel;
}