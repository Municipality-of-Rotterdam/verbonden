-- Update only the seeded ceremony choices; retain titles and descriptions changed in administration.
UPDATE huwelijkstypen
SET tekst = 'U kiest een datum op een maandagochtend.
De ceremonie is in de Felicitatiezaal in het stadhuis.
U neemt 2 getuigen mee. Andere gasten kunnen niet komen.
De gemeente kiest de trouwambtenaar.
De trouwambtenaar houdt geen toespraak.
De ceremonie duurt 10 minuten.'
WHERE soort = 'KLEIN'
  AND titel = 'Gratis';

UPDATE huwelijkstypen
SET tekst = 'U kiest zelf een datum uit de beschikbare data.
De ceremonie is in de Felicitatiezaal in het stadhuis.
Er mogen maximaal 16 mensen bij zijn, u en uw partner meegeteld.
De gemeente kiest de trouwambtenaar.
De trouwambtenaar houdt een toespraak.
De ceremonie duurt 15 minuten.'
WHERE soort = 'MIDDELGROOT'
  AND titel = 'Eenvoudig';

UPDATE huwelijkstypen
SET
    tekst = 'U kiest zelf een datum uit de beschikbare data.
U kiest een trouwzaal in het stadhuis of een eigen locatie.
Hoeveel mensen er mogen komen, hangt af van de locatie.
U mag aangeven welke trouwambtenaar u wilt.
De ceremonie duurt 30 minuten tot 1 uur.'
WHERE soort = 'GROOT'
  AND titel = 'Standaard';
