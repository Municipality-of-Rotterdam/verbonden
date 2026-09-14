UPDATE extras
SET is_active = FALSE
WHERE type = 'internationaleAkte';

INSERT INTO extras (type, naam, omschrijving, afbeelding, prijs, startdatum, einddatum, is_active)
SELECT 'internationaleAkte', 'Internationale huwelijksakte', NULL, NULL, NULL, NULL, NULL, TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM extras
    WHERE type = 'internationaleAkte'
      AND naam = 'Internationale huwelijksakte'
);

UPDATE extras
SET is_active = TRUE,
    omschrijving = NULL,
    afbeelding = NULL,
    prijs = NULL,
    startdatum = NULL,
    einddatum = NULL
WHERE type = 'internationaleAkte'
  AND naam = 'Internationale huwelijksakte';

UPDATE huwelijksdossiers
SET internationale_akte_id = (
    SELECT id
    FROM extras
    WHERE type = 'internationaleAkte'
      AND naam = 'Internationale huwelijksakte'
      AND is_active = TRUE
    ORDER BY id DESC
    LIMIT 1
)
WHERE internationale_akte_id IS NOT NULL;
