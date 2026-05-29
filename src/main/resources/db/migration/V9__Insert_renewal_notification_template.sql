SET FOREIGN_KEY_CHECKS = 0;

-- Renewal Success Template
INSERT INTO `notification_templates` (`id`, `created_at`, `name`, `template_body`, `channel_id`)
VALUES (7, NOW(), 'MEMBER_RENEWAL_CONFIRMATION', '🎉 *Subscription Renewed Successfully!*\n\nHello {{name}},\n\nThank you for renewing your membership with {{gymName}}! We are thrilled to continue supporting you on your fitness journey. 💪\n\n📋 *Updated Plan Details:*\n🔹 *Plan:* {{planName}}\n📅 *New Start Date:* {{startDate}}\n⏳ *New Expiry Date:* {{expiryDate}}\n\nKeep crushing your goals!\n\nBest regards,\nTeam {{gymName}}', 1);

SET FOREIGN_KEY_CHECKS = 1;