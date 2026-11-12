ALTER TABLE trouwlocaties
    ADD COLUMN vrije_locatie BOOLEAN NOT NULL DEFAULT FALSE;

CREATE UNIQUE INDEX uq_trouwlocaties_vrije_locatie
    ON trouwlocaties (vrije_locatie)
    WHERE vrije_locatie;

ALTER TABLE afspraken
    ADD COLUMN locatie_prijs NUMERIC(10, 2);

INSERT INTO trouwlocaties (naam, omschrijving, vrije_locatie)
SELECT 'Vrije locatie',
       'De ceremonie vindt plaats op een locatie die u zelf kiest. De locatiegegevens worden per dossier vastgelegd.',
       TRUE
WHERE NOT EXISTS (SELECT 1 FROM trouwlocaties WHERE vrije_locatie);

INSERT INTO trouwlocaties_beschikbaarheden
    (locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd,
     duur_in_minuten, prijs, ingangsdatum, einddatum)
SELECT l.id, 'REGULIER', tijden.dag, tijden.van, tijden.tot, 60, tijden.prijs,
       DATE '2016-01-01', DATE '2026-12-31'
FROM trouwlocaties l
CROSS JOIN (VALUES
    ('MONDAY',    TIME '00:00', TIME '09:00', 2168.60),
    ('MONDAY',    TIME '09:00', TIME '17:00',  770.90),
    ('MONDAY',    TIME '17:00', TIME '21:00', 1084.30),
    ('MONDAY',    TIME '21:00', TIME '23:59', 2168.60),
    ('TUESDAY',   TIME '00:00', TIME '09:00', 2168.60),
    ('TUESDAY',   TIME '09:00', TIME '17:00',  770.90),
    ('TUESDAY',   TIME '17:00', TIME '21:00', 1084.30),
    ('TUESDAY',   TIME '21:00', TIME '23:59', 2168.60),
    ('WEDNESDAY', TIME '00:00', TIME '09:00', 2168.60),
    ('WEDNESDAY', TIME '09:00', TIME '17:00',  770.90),
    ('WEDNESDAY', TIME '17:00', TIME '21:00', 1084.30),
    ('WEDNESDAY', TIME '21:00', TIME '23:59', 2168.60),
    ('THURSDAY',  TIME '00:00', TIME '09:00', 2168.60),
    ('THURSDAY',  TIME '09:00', TIME '17:00',  770.90),
    ('THURSDAY',  TIME '17:00', TIME '21:00', 1084.30),
    ('THURSDAY',  TIME '21:00', TIME '23:59', 2168.60),
    ('FRIDAY',    TIME '00:00', TIME '09:00', 2168.60),
    ('FRIDAY',    TIME '09:00', TIME '17:00',  770.90),
    ('FRIDAY',    TIME '17:00', TIME '21:00', 1084.30),
    ('FRIDAY',    TIME '21:00', TIME '23:59', 2168.60),
    ('SATURDAY',  TIME '00:00', TIME '09:00', 2168.60),
    ('SATURDAY',  TIME '09:00', TIME '21:00', 1084.30),
    ('SATURDAY',  TIME '21:00', TIME '23:59', 2168.60),
    ('SUNDAY',    TIME '00:00', TIME '23:59', 2168.60)
) AS tijden(dag, van, tot, prijs)
WHERE l.vrije_locatie;
