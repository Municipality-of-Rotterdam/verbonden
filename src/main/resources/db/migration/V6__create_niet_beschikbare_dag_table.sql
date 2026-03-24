CREATE TABLE locatie_niet_beschikbare_dag (
    id                  BIGSERIAL    PRIMARY KEY,
    locatie_id          BIGINT       NOT NULL REFERENCES trouwlocatie (id),
    datum               DATE         NOT NULL,
    reden               TEXT         NOT NULL,
    laatste_wijzig_datum TIMESTAMP   NOT NULL,
    userid              VARCHAR(255) NOT NULL
);
