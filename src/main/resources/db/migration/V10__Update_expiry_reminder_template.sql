SET FOREIGN_KEY_CHECKS = 0;

-- Update the existing template with a high-urgency, clear markdown layout
UPDATE `notification_templates`
SET `template_body` = '⚠️ *Gym Membership Expiring Tomorrow!*\n\nHello {{name}},\n\nThis is a friendly reminder that your membership at {{gymName}} is set to expire *tomorrow* (📅 {{expiryDate}}).\n\nTo ensure your daily workout sessions remain completely uninterrupted, please visit the counter or drop us a message to renew your package.\n\nThank you for being a valued part of our fitness family! Let\'s keep that momentum going! 💪\n\nBest regards,\nTeam {{gymName}}'
WHERE `name` = 'EXPIRY_REMINDER';

SET FOREIGN_KEY_CHECKS = 1;