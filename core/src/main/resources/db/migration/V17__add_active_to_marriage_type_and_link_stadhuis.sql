-- Add active flag to marriage_type.
-- KLEIN is active (enabled on intake page), MIDDELGROOT and GROOT are not yet active.
ALTER TABLE marriage_type
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE marriage_type SET active = FALSE WHERE soort IN ('MIDDELGROOT', 'GROOT');

-- Link 'Stadhuis Rotterdam' (the first location inserted in V4) to the KLEIN marriage type.
INSERT INTO marriage_type_location (marriage_type_id, locatie_id)
SELECT mt.id, tl.id
FROM marriage_type mt
         JOIN trouwlocatie tl ON tl.naam = 'Stadhuis Rotterdam'
WHERE mt.soort = 'KLEIN'
ON CONFLICT (marriage_type_id) DO NOTHING;
