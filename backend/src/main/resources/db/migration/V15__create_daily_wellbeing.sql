CREATE TABLE daily_wellbeing (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    entry_date DATE NOT NULL,
    water_glasses INTEGER NOT NULL DEFAULT 0,
    prenatal_vitamin_taken BOOLEAN NOT NULL DEFAULT FALSE,
    activity_minutes INTEGER NOT NULL DEFAULT 0,
    mood VARCHAR(40),
    sleep_hours NUMERIC(3, 1) NOT NULL DEFAULT 0,
    CONSTRAINT uq_daily_wellbeing_user_date UNIQUE (user_id, entry_date)
);

CREATE INDEX idx_daily_wellbeing_user_date ON daily_wellbeing(user_id, entry_date);