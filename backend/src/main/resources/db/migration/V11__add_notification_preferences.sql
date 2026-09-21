ALTER TABLE users
    ADD COLUMN appointment_reminders_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN browser_notifications_enabled BOOLEAN NOT NULL DEFAULT FALSE;