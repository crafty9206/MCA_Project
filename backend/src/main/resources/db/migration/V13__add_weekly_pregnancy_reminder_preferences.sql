ALTER TABLE users
    ADD COLUMN weekly_pregnancy_reminders_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN weekly_pregnancy_reminder_day INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN weekly_pregnancy_reminder_time TIME NOT NULL DEFAULT '09:00:00';