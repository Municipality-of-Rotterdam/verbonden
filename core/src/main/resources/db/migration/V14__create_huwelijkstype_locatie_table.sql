CREATE TABLE huwelijkstype_locatie (
    huwelijkstype VARCHAR(20) PRIMARY KEY,
    locatie_id    BIGINT NOT NULL REFERENCES trouwlocatie(id)
);

-- GRATIS (KLEIN) and EENVOUDIG (MIDDELGROOT) each have a fixed location.
-- REGULIER (GROOT) has no fixed location: the citizen may choose a custom venue.
INSERT INTO huwelijkstype_locatie (huwelijkstype, locatie_id) VALUES
    ('GRATIS',    1),
    ('EENVOUDIG', 1);
