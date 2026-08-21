CREATE TABLE marriage_type_location (
    marriage_type_id BIGINT PRIMARY KEY REFERENCES marriage_type(id) ON DELETE CASCADE,
    locatie_id       BIGINT NOT NULL REFERENCES trouwlocatie(id)
);

-- Migrate any existing rows from the old table.
-- The old table used HuwelijksType enum values (GRATIS/EENVOUDIG/REGULIER);
-- the new table uses marriage_type.id. Map using the soort column:
--   GRATIS    ↔ KLEIN
--   EENVOUDIG ↔ MIDDELGROOT
--   REGULIER  ↔ GROOT
INSERT INTO marriage_type_location (marriage_type_id, locatie_id)
SELECT mt.id, htl.locatie_id
FROM huwelijkstype_locatie htl
JOIN marriage_type mt ON
    (htl.huwelijkstype = 'GRATIS'    AND mt.soort = 'KLEIN')    OR
    (htl.huwelijkstype = 'EENVOUDIG' AND mt.soort = 'MIDDELGROOT') OR
    (htl.huwelijkstype = 'REGULIER'  AND mt.soort = 'GROOT');

DROP TABLE huwelijkstype_locatie;
