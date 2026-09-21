ALTER TABLE users
    ADD COLUMN missed_task_reminders_enabled BOOLEAN NOT NULL DEFAULT FALSE;