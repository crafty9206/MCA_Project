CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE,
    provider_name VARCHAR(160),
    clinic_name VARCHAR(200),
    notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_appointments_user_starts_at ON appointments(user_id, starts_at);

CREATE TABLE care_tasks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(160) NOT NULL,
    task_date DATE NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_care_tasks_user_date_title UNIQUE (user_id, task_date, title)
);

CREATE INDEX idx_care_tasks_user_date ON care_tasks(user_id, task_date);