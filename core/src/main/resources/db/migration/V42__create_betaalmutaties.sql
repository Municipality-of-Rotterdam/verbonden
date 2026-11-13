-- Een bedrag dat het paar na het indienen nog moet betalen, bijvoorbeeld voor een bijbesteld trouwboekje of alsnog
-- een internationale akte. Een medewerker voegt het toe; de burger betaalt het online. Betaald is een mutatie zodra er
-- een gelukte betaling naar verwijst.
CREATE TABLE betaalmutaties
(
    id              BIGSERIAL PRIMARY KEY,
    dossier_id      BIGINT         NOT NULL REFERENCES huwelijksdossiers (id) ON DELETE CASCADE,
    omschrijving    VARCHAR(100)   NOT NULL,
    bedrag          NUMERIC(10, 2) NOT NULL,
    aangemaakt_door VARCHAR(255)   NOT NULL,
    aangemaakt_op   TIMESTAMP      NOT NULL
);

CREATE INDEX betaalmutaties_dossier_id_idx ON betaalmutaties (dossier_id);

-- Een betaling van soort MUTATIE hoort bij precies één betaalmutatie
ALTER TABLE betalingen
    ADD COLUMN betaalmutatie_id BIGINT REFERENCES betaalmutaties (id);

CREATE INDEX betalingen_betaalmutatie_id_idx ON betalingen (betaalmutatie_id);
