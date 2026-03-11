package com.gymflow.gymflow.notification.dto.response;

import com.gymflow.gymflow.notification.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NotificationResponse {
    private Long eventId;
    private NotificationStatus status;
    private String message;
}
