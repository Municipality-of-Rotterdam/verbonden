-- Seed the marriage_type table with the three ceremony types shown on the intake page.
-- Data is taken from the WicketApplication.properties i18n file (intake.soort.* keys).

INSERT INTO marriage_type (soort, titel, tekst, prijs, url, aangemaakt_op)
VALUES
    (
        'KLEIN',
        'Klein',
        'Maximaal 8 personen in de zaal
Geen voorkeur opgeven trouwambtenaar
Datum kun je kiezen (mits beschikbaar)
Duur van 10 minuten',
        0.00,
        'https://www.rotterdam.nl/trouwen',
        NOW()
    ),
    (
        'MIDDELGROOT',
        'Middelgroot',
        'Maximaal 16 personen in de zaal
Geen voorkeur opgeven trouwambtenaar
Datum kun je kiezen (mits beschikbaar)
Duur van 15 minuten',
        216.60,
        'https://www.rotterdam.nl/trouwen',
        NOW()
    ),
    (
        'GROOT',
        'Groot',
        'Geen maximum aantal gasten
Maximaal 70 zitplaatsen
Voorkeur trouwambtenaar opgeven (max 3 opties)
Eigen locatie mogelijk
Eigen trouwambtenaar mogelijk
Datum kun je kiezen (mits beschikbaar)
Duur vanaf 30 minuten tot maximaal 1 uur',
        624.40,
        'https://www.rotterdam.nl/trouwen',
        NOW()
    )
ON CONFLICT (soort) DO NOTHING;
