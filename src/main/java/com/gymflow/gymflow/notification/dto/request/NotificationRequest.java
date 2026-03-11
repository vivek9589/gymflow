package com.gymflow.gymflow.notification.dto.request;

import com.gymflow.gymflow.notification.enums.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Long memberId;
    private Long templateId;
    private ChannelType channel;
}