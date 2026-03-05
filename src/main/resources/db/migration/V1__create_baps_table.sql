CREATE TABLE baps (
    id                BIGSERIAL PRIMARY KEY,
    naam              VARCHAR(255) NOT NULL,
    foto_url          TEXT,
    hobbies           TEXT,
    beschrijving      TEXT,
    actief            BOOLEAN      NOT NULL DEFAULT TRUE,
    actief_vanaf      DATE,
    actief_tot_en_met DATE,
    beschikbare_dagen TEXT,
    aangemaakt_op     TIMESTAMP    NOT NULL DEFAULT NOW()
);
