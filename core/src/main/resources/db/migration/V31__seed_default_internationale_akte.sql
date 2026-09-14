UPDATE extras
SET is_active = FALSE
WHERE type = 'internationaleAkte';

INSERT INTO extras (type, naam, omschrijving, afbeelding, prijs, startdatum, einddatum, is_active)
VALUES ('internationaleAkte', 'Internationale huwelijksakte', NULL, NULL, NULL, NULL, NULL, TRUE);

UPDATE huwelijksdossiers
SET internationale_akte_id = (
    SELECT id
    FROM extras
    WHERE type = 'internationaleAkte'
      AND is_active = TRUE
    ORDER BY id DESC
    LIMIT 1
)
WHERE internationale_akte_id IS NOT NULL;
