package com.gymflow.gymflow.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "message_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long gymId;

    private Long memberId;

    private String channel;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String providerMessageId;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String response;

    private LocalDateTime sentAt;
}