CREATE TABLE marriage_type (
    id            BIGSERIAL    PRIMARY KEY,
    titel         VARCHAR(255) NOT NULL,
    tekst         TEXT         NOT NULL,
    prijs         NUMERIC(10, 2) NOT NULL,
    url           TEXT         NOT NULL,
    aangemaakt_op TIMESTAMP    NOT NULL DEFAULT NOW()
);
