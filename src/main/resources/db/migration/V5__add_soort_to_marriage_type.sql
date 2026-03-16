ALTER TABLE marriage_type
    ADD COLUMN soort VARCHAR(50) NOT NULL DEFAULT 'FREE',
    ADD CONSTRAINT marriage_type_soort_unique UNIQUE (soort);

ALTER TABLE marriage_type ALTER COLUMN soort DROP DEFAULT;
