-- Update only the seeded ceremony choices; retain titles and descriptions changed in administration.
UPDATE huwelijkstypen
SET titel = 'Gratis',
    tekst = 'U kiest zelf een datum. De ceremonie is op maandagochtend.
De ceremonie is in de felicitatiezaal in het stadhuis.
U neemt 2 getuigen mee. Andere gasten zijn niet welkom.
U kiest de trouwambtenaar niet zelf.
De trouwambtenaar houdt geen toespraak.
De ceremonie duurt 10 minuten.'
WHERE soort = 'KLEIN'
  AND titel = 'Klein'
  AND tekst = 'Maximaal 8 personen in de zaal
U kunt geen trouwambtenaar kiezen
U kiest zelf de datum (als die vrij is)
Duur van 10 minuten';

UPDATE huwelijkstypen
SET titel = 'Eenvoudig',
    tekst = 'U kiest zelf een datum uit de beschikbare data.
De ceremonie is in de felicitatiezaal in het stadhuis.
Er zijn maximaal 16 personen, inclusief u en uw partner.
U kiest de trouwambtenaar niet zelf.
De trouwambtenaar houdt een toespraak.
De ceremonie duurt 15 minuten.'
WHERE soort = 'MIDDELGROOT'
  AND titel = 'Middelgroot'
  AND tekst = 'Maximaal 16 personen in de zaal
U kunt geen trouwambtenaar kiezen
U kiest zelf de datum (als die vrij is)
Duur van 15 minuten';

UPDATE huwelijkstypen
SET titel = 'Standaard',
    tekst = 'U kiest zelf een datum uit de beschikbare data.
U kiest een trouwzaal in het stadhuis of een eigen locatie.
Het aantal personen hangt af van de locatie die u kiest.
U kunt uw voorkeur voor een trouwambtenaar aangeven.
De ceremonie duurt 30 minuten tot 1 uur.'
WHERE soort = 'GROOT'
  AND titel = 'Groot'
  AND tekst = 'Geen maximum aantal gasten
Maximaal 70 zitplaatsen
Voorkeur trouwambtenaar opgeven (max 3 opties)
Eigen locatie mogelijk
Eigen trouwambtenaar mogelijk
U kiest zelf de datum (als die vrij is)
Duur vanaf 30 minuten tot maximaal 1 uur';
