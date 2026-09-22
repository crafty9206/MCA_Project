ALTER TABLE task_details
    ADD COLUMN recurrence VARCHAR(20) NOT NULL DEFAULT 'NONE',
    ADD COLUMN recurrence_start_date DATE;