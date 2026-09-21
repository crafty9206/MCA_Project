CREATE TABLE task_definitions (
    id UUID PRIMARY KEY,
    title VARCHAR(160) NOT NULL UNIQUE,
    details VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE care_tasks ADD COLUMN definition_id UUID REFERENCES task_definitions(id);
CREATE INDEX idx_care_tasks_definition ON care_tasks(definition_id);