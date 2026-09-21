-- Run once in MySQL for an existing database created before task_details.
-- Stop the backend before running this script.

USE maatricare;

RENAME TABLE task_definitions TO task_details;

ALTER TABLE task_details
    DROP COLUMN details,
    DROP COLUMN active;

ALTER TABLE care_tasks
    DROP FOREIGN KEY fk_care_tasks_definition,
    DROP INDEX idx_care_tasks_definition,
    DROP INDEX uq_care_tasks_user_date_definition,
    CHANGE COLUMN definition_id task_detail_id BINARY(16) NULL;

INSERT IGNORE INTO task_details (id, title)
SELECT UUID_TO_BIN(UUID()), title
FROM care_tasks
WHERE title IS NOT NULL
GROUP BY title;

UPDATE care_tasks care
JOIN task_details details ON details.title = care.title
SET care.task_detail_id = details.id
WHERE care.task_detail_id IS NULL;

ALTER TABLE care_tasks
    DROP COLUMN title,
    MODIFY task_detail_id BINARY(16) NOT NULL,
    ADD CONSTRAINT fk_care_tasks_task_detail
        FOREIGN KEY (task_detail_id) REFERENCES task_details(id),
    ADD CONSTRAINT uq_care_tasks_user_date_task_detail
        UNIQUE (user_id, task_date, task_detail_id),
    ADD INDEX idx_care_tasks_task_detail (task_detail_id);

INSERT IGNORE INTO task_details (id, title)
VALUES
    (UUID_TO_BIN(UUID()), 'Take prenatal vitamins'),
    (UUID_TO_BIN(UUID()), 'Drink 8 glasses of water'),
    (UUID_TO_BIN(UUID()), 'Take a 20 minute walk'),
    (UUID_TO_BIN(UUID()), 'Get enough rest');

-- care_tasks remains the per-user status table:
-- user_id, task_detail_id, task_date, completed.
