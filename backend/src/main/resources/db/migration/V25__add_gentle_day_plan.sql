ALTER TABLE daily_wellbeing
    ADD COLUMN energy_level VARCHAR(16) NOT NULL DEFAULT 'STEADY',
    ADD COLUMN focus_task_count INT NOT NULL DEFAULT 3;