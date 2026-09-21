ALTER TABLE task_definitions RENAME TO task_details;
ALTER TABLE care_tasks RENAME COLUMN definition_id TO task_detail_id;

INSERT INTO task_details (id, title)
SELECT md5(random()::text || clock_timestamp()::text)::uuid, title
FROM care_tasks
WHERE title IS NOT NULL
ON CONFLICT (title) DO NOTHING;

UPDATE care_tasks care
SET task_detail_id = details.id
FROM task_details details
WHERE care.task_detail_id IS NULL AND details.title = care.title;

ALTER TABLE care_tasks DROP CONSTRAINT uq_care_tasks_user_date_title;
ALTER TABLE care_tasks DROP COLUMN title;
ALTER TABLE care_tasks ALTER COLUMN task_detail_id SET NOT NULL;
ALTER TABLE care_tasks ADD CONSTRAINT uq_care_tasks_user_date_task_detail
	UNIQUE (user_id, task_date, task_detail_id);