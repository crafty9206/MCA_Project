-- Use this repair when task_details and task_detail_id already exist but
-- care_tasks still has the legacy NOT NULL title column.

USE maatricare;

INSERT IGNORE INTO task_details (id, title)
SELECT UUID_TO_BIN(UUID()), title
FROM care_tasks
WHERE title IS NOT NULL
GROUP BY title;

UPDATE care_tasks care
JOIN task_details details ON details.title = care.title
SET care.task_detail_id = details.id
WHERE care.task_detail_id IS NULL;

SET @legacy_indexes = (
    SELECT GROUP_CONCAT(CONCAT('DROP INDEX `', index_name, '`') SEPARATOR ', ')
    FROM (
        SELECT DISTINCT index_name
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'care_tasks'
          AND column_name = 'title'
          AND index_name <> 'PRIMARY'
    ) indexes_with_title
);
SET @drop_indexes_sql = IF(
    @legacy_indexes IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE care_tasks ', @legacy_indexes)
);
PREPARE drop_indexes_statement FROM @drop_indexes_sql;
EXECUTE drop_indexes_statement;
DEALLOCATE PREPARE drop_indexes_statement;

ALTER TABLE care_tasks
    DROP COLUMN title,
    MODIFY task_detail_id BINARY(16) NOT NULL;