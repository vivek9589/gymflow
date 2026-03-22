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
        variables.put("{{name}}", member.getName());
        variables.put("{{gymName}}", member.getGym().getName());
        variables.put("{{expiryDate}}",
                member.getExpiryDate() != null ? member.getExpiryDate().toString() : "");

        // Optional placeholders for holiday reminder
        // These can be set dynamically when you prepare the template
        // For example, if you pass holiday info separately
        variables.put("{{holidayName}}", "");
        variables.put("{{holidayDate}}", "");

        // Optional contact number placeholder (if you store it in Gym or Config)
        variables.put("{{contactNumber}}",
                member.getGym().getContactNumber() != null ? member.getGym().getContactNumber() : "");

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }

        return message;
    }
}