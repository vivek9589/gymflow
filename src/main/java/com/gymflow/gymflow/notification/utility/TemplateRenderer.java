package com.gymflow.gymflow.notification.utility;

import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;

public class TemplateRenderer {

    private TemplateRenderer() {}

    public static String render(NotificationTemplate template, Member member) {
        String msg = template.getTemplateBody();
        msg = msg.replace("{{name}}", member.getName());
        msg = msg.replace("{{expiry_date}}",
                member.getExpiryDate() != null ? member.getExpiryDate().toString() : "");
        msg = msg.replace("{{gym_name}}", member.getGym().getName());
        return msg;
    }
}