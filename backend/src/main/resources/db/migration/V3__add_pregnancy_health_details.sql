ALTER TABLE pregnancy_profiles
    ADD COLUMN age_years INTEGER,
    ADD COLUMN height_cm NUMERIC(5, 1),
    ADD COLUMN pre_pregnancy_weight_kg NUMERIC(5, 1),
    ADD COLUMN blood_pressure VARCHAR(20),
    ADD COLUMN blood_group VARCHAR(3);
