CREATE TABLE vrije_trouwlocaties (
    id              BIGSERIAL PRIMARY KEY,
    dossier_id      BIGINT NOT NULL UNIQUE REFERENCES huwelijksdossiers (id),
    naam            VARCHAR(255) NOT NULL,
    adres           VARCHAR(500) NOT NULL,
    telefoonnummer  VARCHAR(30) NOT NULL,
    contactpersoon  VARCHAR(255),
    goedgekeurd     BOOLEAN NOT NULL DEFAULT FALSE
);
