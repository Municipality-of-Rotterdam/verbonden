CREATE TABLE huwelijksdossiers_getuigen (
    id         BIGSERIAL PRIMARY KEY,
    dossier_id BIGINT       NOT NULL REFERENCES huwelijksdossiers (id),
    volgnummer INT          NOT NULL,
    naam       VARCHAR(500),
    bestand_naam VARCHAR(500),
    bestand_data BYTEA,
    CONSTRAINT uq_getuige_dossier_volgnummer UNIQUE (dossier_id, volgnummer)
);
