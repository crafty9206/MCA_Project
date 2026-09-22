CREATE TABLE symptom_entries (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    symptom VARCHAR(80) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    notes VARCHAR(1000)
);

CREATE INDEX idx_symptom_entries_user_occurred_at ON symptom_entries(user_id, occurred_at);