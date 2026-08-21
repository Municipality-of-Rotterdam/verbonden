
TRUNCATE TABLE trouwlocatie CASCADE;
--TRUNCATE TABLE locatie_beschikbaarheid;

-- Initiële vulling: Rotterdam trouwlocaties (bron: rotterdam.nl/trouwlocatie-en-ceremonie)
INSERT INTO trouwlocatie (naam, foto_url) VALUES
    ('Trouwzaal1',
     ''),
    ('Trouwzaal2',
     ''),
    ('Burgerzaal',
     ''),
    ('Felicitatiekamer',
     '');


INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Felicitatiekamer'), 'GRATIS', 'MONDAY', '09:00', '11:50', 10, 0.00,
        '2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Felicitatiekamer'), 'EENVOUDIG', 'TUESDAY', '10:15', '12:15', 15, 216.60,
        '2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Felicitatiekamer'), 'EENVOUDIG', 'WEDNESDAY', '10:15', '12:15', 15, 216.60,
        '2026-01-01', '2026-12-31');




INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal1'), 'REGULIER', 'MONDAY', '10:00', '11:30', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal1'), 'REGULIER', 'TUESDAY', '10:00', '11:30', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal1'), 'REGULIER', 'WEDNESDAY', '10:00', '11:30', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal1'), 'REGULIER', 'MONDAY', '14:00', '16:00', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal1'), 'REGULIER', 'TUESDAY', '14:00', '16:00', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal1'), 'REGULIER', 'WEDNESDAY', '14:00', '16:00', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal1'), 'REGULIER', 'THURSDAY', '09:00', '12:00', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal1'), 'REGULIER', 'FRIDAY', '09:00', '12:00', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal1'), 'REGULIER', 'THURSDAY', '13:30', '16:00', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal1'), 'REGULIER', 'FRIDAY', '13:30', '16:00', 30, 624.40,'2026-01-01', '2026-12-31');


INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal2'), 'REGULIER', 'MONDAY', '14:15', '16:15', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal2'), 'REGULIER', 'TUESDAY', '14:15', '16:15', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal2'), 'REGULIER', 'WEDNESDAY', '14:15', '16:15', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal2'), 'REGULIER', 'THURSDAY', '09:15', '11:45', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal2'), 'REGULIER', 'FRIDAY', '09:15', '11:45', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal2'), 'REGULIER', 'THURSDAY', '13:45', '16:15', 30, 624.40,'2026-01-01', '2026-12-31');

INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Trouwzaal2'), 'REGULIER', 'FRIDAY', '13:45', '16:15', 30, 624.40,'2026-01-01', '2026-12-31');


DO $$
DECLARE
    locatie RECORD;
    dag     TEXT;
BEGIN
    FOR locatie IN SELECT id FROM trouwlocatie WHERE naam='Burgerzaal'  LOOP

        -- REGULIER: maandag t/m zaterdag
        FOREACH dag IN ARRAY ARRAY['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY'] LOOP
            INSERT INTO locatie_beschikbaarheid
                (locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd,
                 duur_in_minuten, prijs, ingangsdatum, einddatum)
            VALUES (locatie.id, 'REGULIER', dag, '09:00', '12:00', 30, 981.50, '2026-01-01', '2026-12-31'),
                   (locatie.id, 'REGULIER', dag, '14:00', '16:00', 30, 981.50, '2026-01-01', '2026-12-31');
        END LOOP;

    END LOOP;
END $$;


INSERT INTO locatie_beschikbaarheid
(locatie_id, huwelijkstype, dag_van_de_week, start_tijd, eind_tijd, duur_in_minuten, prijs, ingangsdatum, einddatum)
VALUES ((select id from trouwlocatie where naam='Burgerzaal'), 'REGULIER', 'SATURDAY', '13:45', '14:00', 30, 1429.10,'2026-01-01', '2026-12-31');


truncate table marriage_type_location;

INSERT INTO marriage_type_location (marriage_type_id, locatie_id)
SELECT mt.id, tl.id
FROM marriage_type mt
         JOIN trouwlocatie tl ON tl.naam = 'Felicitatiekamer'
WHERE mt.soort <> 'GROOT'
    ON CONFLICT (marriage_type_id) DO NOTHING;