-- Herschrijf de geseede kenmerken van de huwelijkstypen (V16) naar taalniveau B1 en de aanspreekvorm u.
-- Alleen de letterlijke seed-zinnen worden vervangen, zodat teksten die in beheer zijn aangepast blijven staan.
UPDATE huwelijkstypen
SET tekst = replace(
        replace(tekst, 'Geen voorkeur opgeven trouwambtenaar', 'U kunt geen trouwambtenaar kiezen'),
        'Datum kun je kiezen (mits beschikbaar)', 'U kiest zelf de datum (als die vrij is)');
