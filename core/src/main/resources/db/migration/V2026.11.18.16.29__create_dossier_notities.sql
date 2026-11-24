CREATE TABLE dossier_notities
(
    id             BIGSERIAL PRIMARY KEY,
    dossier_id     BIGINT       NOT NULL REFERENCES huwelijksdossiers (id) ON DELETE CASCADE,
    tekst          VARCHAR(1000) NOT NULL,
    gebruikersnaam VARCHAR(255) NOT NULL,
    aangemaakt_op  TIMESTAMP    NOT NULL
);

CREATE INDEX dossier_notities_dossier_aangemaakt_idx
    ON dossier_notities (dossier_id, aangemaakt_op DESC, id DESC);
