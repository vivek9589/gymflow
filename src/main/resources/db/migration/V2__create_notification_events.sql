-- V3__create_notification_events.sql

CREATE TABLE notification_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    error_message TEXT,
    gym_id BIGINT,
    member_id BIGINT,
    processed_at TIMESTAMP NULL,
    status VARCHAR(50),
    template_id BIGINT
);

-- Optional: add indexes for faster lookups
CREATE INDEX idx_notification_events_gym_id ON notification_events(gym_id);
CREATE INDEX idx_notification_events_member_id ON notification_events(member_id);
CREATE INDEX idx_notification_events_template_id ON notification_events(template_id);