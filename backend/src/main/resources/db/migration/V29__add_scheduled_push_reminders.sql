ALTER TABLE users ADD COLUMN timezone_id VARCHAR(64) NOT NULL DEFAULT 'Asia/Kolkata';
ALTER TABLE users ADD COLUMN push_notifications_enabled BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE push_subscriptions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    endpoint VARCHAR(2048) NOT NULL,
    endpoint_hash VARCHAR(64) NOT NULL,
    p256dh_key VARCHAR(255) NOT NULL,
    auth_key VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_push_subscriptions_user_endpoint UNIQUE (user_id, endpoint_hash)
);

CREATE TABLE push_reminders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    deduplication_key VARCHAR(300) NOT NULL,
    reminder_type VARCHAR(20) NOT NULL,
    title VARCHAR(160) NOT NULL,
    body VARCHAR(500) NOT NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    snoozed_until TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    attempt_count INTEGER NOT NULL DEFAULT 0,
    claimed_at TIMESTAMP WITH TIME ZONE,
    snooze_token_hash VARCHAR(64),
    snooze_token_expires_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_push_reminders_user_key UNIQUE (user_id, deduplication_key)
);

CREATE INDEX idx_push_reminders_due ON push_reminders(status, scheduled_at);