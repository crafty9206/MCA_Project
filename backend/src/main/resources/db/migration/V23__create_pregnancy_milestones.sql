CREATE TABLE pregnancy_milestones (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    week_number INT NOT NULL,
    type VARCHAR(16) NOT NULL DEFAULT 'STANDARD',
    completed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_pregnancy_milestones_user_week ON pregnancy_milestones(user_id, week_number);
