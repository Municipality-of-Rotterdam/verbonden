-- Sample data voor lokaal gebruik (alleen app-local): de vijf trouwboekjes met afbeelding en
-- fictieve prijzen. De afbeeldingen staan in static/trouwboekjes en zijn uitgesneden uit de foto's
-- op rotterdam.nl/media/8054 t/m 8058.
-- Repeatable migratie: voegt alleen ontbrekende trouwboekjes toe, zodat wijzigingen via beheer blijven staan.

INSERT INTO trouwboekjes (naam, omschrijving, afbeelding, prijs)
SELECT sample.naam, sample.omschrijving, sample.afbeelding, sample.prijs
FROM (VALUES
          ('Rood & goud', 'Leer', '/trouwboekjes/rood.webp', 25.60),
          ('Zwart', 'Leer', '/trouwboekjes/zwart.webp', 40.50),
          ('Wit & goud', 'Leer', '/trouwboekjes/wit.webp', 31.50),
          ('Blauw & zilver', 'Leer', '/trouwboekjes/blauw.webp', 40.50),
          ('Groen & goud', 'Vegan', '/trouwboekjes/groen.webp', 40.50)
     ) AS sample (naam, omschrijving, afbeelding, prijs)
WHERE NOT EXISTS (
    SELECT 1
    FROM trouwboekjes t
    WHERE t.afbeelding = sample.afbeelding
);
