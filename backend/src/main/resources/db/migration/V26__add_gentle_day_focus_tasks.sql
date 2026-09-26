CREATE TABLE daily_wellbeing_focus_tasks (
    daily_wellbeing_id UUID NOT NULL REFERENCES daily_wellbeing(id) ON DELETE CASCADE,
    task_id UUID NOT NULL REFERENCES care_tasks(id) ON DELETE CASCADE,
    sort_order INT NOT NULL,
    PRIMARY KEY (daily_wellbeing_id, task_id)
);

CREATE INDEX idx_daily_wellbeing_focus_tasks_task ON daily_wellbeing_focus_tasks(task_id);