ALTER TABLE users
    ADD COLUMN daily_care_reminders_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN daily_care_reminder_time TIME NOT NULL DEFAULT '09:00:00';