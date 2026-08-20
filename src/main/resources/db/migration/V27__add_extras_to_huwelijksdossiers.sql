ALTER TABLE huwelijksdossiers
    ADD COLUMN ringen_uitwisselen BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN muziek             BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN trouwboekje_id     BIGINT REFERENCES extras (id),
    ADD COLUMN internationale_akte_id BIGINT REFERENCES extras (id);
