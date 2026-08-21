CREATE TABLE extras (
    id          BIGSERIAL PRIMARY KEY,
    type        VARCHAR(50)     NOT NULL,
    naam        VARCHAR(255)    NOT NULL,
    omschrijving TEXT,
    afbeelding  VARCHAR(500),
    prijs       NUMERIC(10, 2),
    startdatum  DATE,
    einddatum   DATE,
    aangemaakt_op TIMESTAMP NOT NULL DEFAULT NOW()
);
