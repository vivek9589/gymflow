-- Disable foreign key checks momentarily to guarantee explicit ID inserts safely
SET FOREIGN_KEY_CHECKS = 0;

-- 1. General Welcome Greeting Template
INSERT INTO `notification_templates` (`id`, `created_at`, `name`, `template_body`, `channel_id`)
VALUES (4, NOW(), 'MEMBER_WELCOME', '🎉 Hello {{name}},\n\nWelcome to {{gymName}}! We are thrilled to have you with us on your fitness journey. Thank you for choosing us! 💪\n\nBest regards,\nTeam {{gymName}}', 1);

-- 2. Subscription Plan Contract Breakdown Template
INSERT INTO `notification_templates` (`id`, `created_at`, `name`, `template_body`, `channel_id`)
VALUES (5, NOW(), 'MEMBER_PLAN_DETAILS', '📋 Plan Details for {{name}},\n\nYou have successfully subscribed to the *{{planName}}* plan.\n\n📅 *Start Date:* {{startDate}}\n⏳ *Expiry Date:* {{expiryDate}}\n\nPlease ensure renewal before expiry to enjoy uninterrupted workouts.\n\nBest regards,\nTeam {{gymName}}', 1);


-- 3. Attendance Hardware Token Key Template (Kept clean since Java now binds the full URL mapping)
INSERT INTO `notification_templates` (`id`, `created_at`, `name`, `template_body`, `channel_id`)
VALUES (6, NOW(), 'MEMBER_ACCESS_PASS', '🔑 *Digital Access Pass*\n\nHello {{name}},\n\nUse this link to mark attendance at {{gymName}}:\n👉 {{checkInToken}}\n\nOnce you open this link, tap on the button to mark the attendance.\n\n⚠️ *Important:* Please do not share your attendance link with anyone.\n\nBest regards,\nTeam {{gymName}}', 1);