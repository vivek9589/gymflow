package com.gymflow.gymflow.notification.utility;

import com.gymflow.gymflow.member.entity.Member;
import com.gymflow.gymflow.notification.entity.NotificationTemplate;

import java.util.HashMap;
import java.util.Map;

public class TemplateRenderer {

    private TemplateRenderer() {}

    public static String render(NotificationTemplate template, Member member) {
        String message = template.getTemplateBody();

        Map<String, String> variables = new HashMap<>();
        variables.put("{{name}}", member.getName() != null ? member.getName() : "");
        variables.put("{{gymName}}", (member.getGym() != null && member.getGym().getName() != null) ? member.getGym().getName() : "");
        variables.put("{{expiryDate}}", member.getExpiryDate() != null ? member.getExpiryDate().toString() : "");
        variables.put("{{planName}}", (member.getCurrentPlan() != null && member.getCurrentPlan().getName() != null) ? member.getCurrentPlan().getName() : "");
        variables.put("{{startDate}}", member.getSubscriptionStartDate() != null ? member.getSubscriptionStartDate().toString() : "");

        // 🔥 FIXED: Concat the full URL route context here dynamically
        String fullCheckInUrl = "https://fitness-zen-desk.vercel.app/checkin/" + (member.getCheckInToken() != null ? member.getCheckInToken() : "");
        variables.put("{{checkInToken}}", fullCheckInUrl);

        variables.put("{{holidayName}}", "");
        variables.put("{{holidayDate}}", "");
        variables.put("{{contactNumber}}", (member.getGym() != null && member.getGym().getContactNumber() != null) ? member.getGym().getContactNumber() : "");

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            if (message == null) break;
            message = message.replace(entry.getKey(), entry.getValue());
        }

        return message;
    }
}