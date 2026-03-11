package com.gymflow.gymflow.notification.entity;

import com.gymflow.gymflow.notification.enums.ChannelType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "notification_channels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private ChannelType type; // WHATSAPP, SMS, EMAIL, N8N
}