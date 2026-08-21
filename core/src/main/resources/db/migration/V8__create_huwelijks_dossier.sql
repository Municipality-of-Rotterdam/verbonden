CREATE TABLE huwelijks_dossier (
    id               BIGSERIAL    PRIMARY KEY,
    registratie_type VARCHAR(40)  NOT NULL,
    ceremonie_soort  VARCHAR(20)  NOT NULL,
    aangemaakt_op    TIMESTAMP    NOT NULL DEFAULT NOW()
);
