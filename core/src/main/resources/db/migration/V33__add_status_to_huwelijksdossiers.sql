ALTER TABLE huwelijksdossiers
    ADD COLUMN status       VARCHAR(20) NOT NULL DEFAULT 'CONCEPT',
    ADD COLUMN ingediend_op TIMESTAMP;

ALTER TABLE huwelijksdossiers
    ADD CONSTRAINT huwelijksdossiers_status_check
        CHECK (status IN ('CONCEPT', 'INGEDIEND', 'GEACCEPTEERD', 'AFGEWEZEN'));
