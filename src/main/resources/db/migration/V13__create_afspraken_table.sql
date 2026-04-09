CREATE TABLE afspraken (
    id          BIGSERIAL PRIMARY KEY,
    dossier_id  BIGINT NOT NULL REFERENCES huwelijks_dossier(id),
    locatie_id  BIGINT NOT NULL REFERENCES trouwlocatie(id),
    datum       DATE   NOT NULL,
    start_tijd  TIME   NOT NULL,
    eind_tijd   TIME   NOT NULL
);
