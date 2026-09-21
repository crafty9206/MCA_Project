CREATE TABLE appointment_questions (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    question VARCHAR(500) NOT NULL,
    answered BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_appointment_questions_appointment ON appointment_questions(appointment_id);