CREATE TABLE trouwlocatie (
    id            BIGSERIAL PRIMARY KEY,
    naam          VARCHAR(255) NOT NULL,
    foto_url      TEXT,
    aangemaakt_op TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE locatie_beschikbaarheid (
    id              BIGSERIAL    PRIMARY KEY,
    locatie_id      BIGINT       NOT NULL REFERENCES trouwlocatie (id),
    huwelijkstype   VARCHAR(20)  NOT NULL,
    dag_van_de_week VARCHAR(20)  NOT NULL,
    start_tijd      TIME         NOT NULL,
    eind_tijd       TIME         NOT NULL,
    duur_in_minuten INT          NOT NULL,
    prijs           NUMERIC(10, 2) NOT NULL,
    ingangsdatum    DATE         NOT NULL,
    einddatum       DATE         NOT NULL
);

-- Initiële vulling: Rotterdam trouwlocaties (bron: rotterdam.nl/trouwlocatie-en-ceremonie)
INSERT INTO trouwlocatie (naam, foto_url) VALUES
    ('Stadhuis Rotterdam',
     'https://www.rotterdam.nl/media/stadhuis-rotterdam-trouwzaal.jpg'),
    ('Historisch Museum Rotterdam – Schielandshuis',
     'https://www.rotterdam.nl/media/schielandshuis-trouwzaal.jpg'),
    ('Maritiem Museum Rotterdam',
     'https://www.rotterdam.nl/media/maritiem-museum-trouwzaal.jpg'),
    ('Euromast',
     'https://www.rotterdam.nl/media/euromast-trouwzaal.jpg'),
    ('De Doelen',
     'https://www.rotterdam.nl/media/de-doelen-trouwzaal.jpg'),
    ('Museum Rotterdam',
     'https://www.rotterdam.nl/media/museum-rotterdam-trouwzaal.jpg'),
    ('Hotel New York',
     'https://www.rotterdam.nl/media/hotel-new-york-trouwzaal.jpg');

-- Initiële vulling: beschikbaarheden (bron: rotterdam.nl/media/7023, prijslijst 2026)
-- Ingangsdatum 1 januari 2026, einddatum 31 december 2026
-- GRATIS: maandag t/m vrijdag, 09:00–09:15, 15 minuten, €0
-- EENVOUDIG: maandag t/m zaterdag, 10:00–10:30, 30 minuten, €165
-- REGULIER: maandag t/m zaterdag, 11:00–11:45, 45 minuten, €495

DO $$
DECLARE
    locatie RECORD;
    dag     TEXT;
BEGIN
    FOR locatie IN SELECT id FROM trouwlocatie LOOP

        -- GRATIS: maandag t/m vrijdag
        FOREACH dag IN ARRAY ARRAY['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY'] LOOP
            INSERT INTO locatie_beschikbaarheid
                (locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd,
                 duur_in_minuten, prijs, ingangsdatum, einddatum)
            VALUES (locatie.id, 'GRATIS', dag, '09:00', '09:15', 15, 0.00,
                    '2026-01-01', '2026-12-31');
        END LOOP;

        -- EENVOUDIG: maandag t/m zaterdag
        FOREACH dag IN ARRAY ARRAY['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY'] LOOP
            INSERT INTO locatie_beschikbaarheid
                (locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd,
                 duur_in_minuten, prijs, ingangsdatum, einddatum)
            VALUES (locatie.id, 'EENVOUDIG', dag, '10:00', '10:30', 30, 165.00,
                    '2026-01-01', '2026-12-31');
        END LOOP;

        -- REGULIER: maandag t/m zaterdag
        FOREACH dag IN ARRAY ARRAY['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY'] LOOP
            INSERT INTO locatie_beschikbaarheid
                (locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd,
                 duur_in_minuten, prijs, ingangsdatum, einddatum)
            VALUES (locatie.id, 'REGULIER', dag, '11:00', '11:45', 45, 495.00,
                    '2026-01-01', '2026-12-31');
        END LOOP;

    END LOOP;
END $$;
