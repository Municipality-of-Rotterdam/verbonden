-- Online betalingen van een dossier. Een dossier kan meer betaalpogingen hebben (afgebroken, opnieuw geprobeerd),
-- en later ook betalingen voor een wijziging (soort MUTATIE).
CREATE TABLE betalingen
(
    id                 BIGSERIAL PRIMARY KEY,
    dossier_id         BIGINT         NOT NULL REFERENCES huwelijksdossiers (id) ON DELETE CASCADE,
    soort              VARCHAR(20)    NOT NULL,
    betalingskenmerk   VARCHAR(30)    NOT NULL UNIQUE,
    bedrag             NUMERIC(10, 2) NOT NULL,
    betaler_bsn        VARCHAR(10)    NOT NULL,
    status             VARCHAR(20)    NOT NULL,
    externe_referentie VARCHAR(100),
    gestart_op         TIMESTAMP      NOT NULL,
    afgerond_op        TIMESTAMP
);

CREATE INDEX betalingen_dossier_id_idx ON betalingen (dossier_id);
