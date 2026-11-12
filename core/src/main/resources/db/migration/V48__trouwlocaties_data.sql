UPDATE public.trouwlocaties SET naam = 'Burgerzaal', foto_url = '/trouwlocaties/burgerzaal.webp', aangemaakt_op = '2026-11-03 14:49:16.226682', omschrijving = e'<ul><li>De trouwzaal is naast de Burgerzaal, op de eerste verdieping van het stadhuis. Er is een lift en de zaal is geschikt voor een rolstoel.</li><li>
In de trouwzaal is plaats voor 60 personen.</li><li>
De ceremonie duurt 60 minuten. Dit is inclusief in- en uitloop, het officiële deel, de toespraak en de felicitaties na afloop.</li><li>
U hebt minimaal 2 getuigen, maximaal 4.</li><li>
U krijgt 2 parkeerkaarten. Voor de andere gasten zijn er genoeg parkeermogelijkheden in de buurt. Dat is wel betaald parkeren.</li><li>
U mag muziek meenemen en afspelen via cd, mp3 of bluetooth. 
De stadsorganist kan ook voor u spelen op de piano of het orgel.</li><li>
U kiest uw top 3 van trouwambtenaren van de gemeente. Of u kiest u eigen trouwambtenaar. 
Kijk voor meer informatie op de pagina Trouwambtenaar.</li><li>
De trouwambtenaar houdt een toespraak. Daarbij mag ook een eigen toespraak. </li>
<ul>

<div class="alert alert-info">Het kan gebeuren dat de Burgerzaal nodig is voor onverwachte situaties. De gemeente zorgt dan in overleg met u voor een andere locatie en doet haar best rekening te houden met uw wensen. </div>', detail_url = 'https://www.rotterdam.nl/trouwen/burgerzaal', vrije_locatie = false WHERE id = 10;
UPDATE public.trouwlocaties SET naam = 'Felicitatiekamer', foto_url = '/trouwlocaties/felicitatiekamer.webp', aangemaakt_op = '2026-11-03 14:49:16.226682', omschrijving = null, detail_url = null, vrije_locatie = false WHERE id = 11;
UPDATE public.trouwlocaties SET naam = 'Trouwzaal 1', foto_url = '/trouwlocaties/trouwzaal1.webp', aangemaakt_op = '2026-11-03 14:49:16.226682', omschrijving = e'<ul>
<li>De trouwzaal is op de begane grond van het stadhuis.
In de trouwzaal is plaats voor 70 personen. </li>
<li>De ceremonie duurt 30 minuten. Dit is inclusief inloop en uitloop, het officiële deel, de toespraak en de felicitaties na afloop in de felicitatiekamer.</li><li>
U hebt minimaal 2 getuigen, maximaal 4.</li><li>
U krijgt 2 parkeerkaarten. Voor de andere gasten zijn er genoeg parkeermogelijkheden in de buurt. Dat is wel betaald parkeren.</li><li>
U mag muziek meenemen en afspelen via cd, mp3 of bluetooth.</li><li>
U kiest uw top 3 van trouwambtenaren van de gemeente. Of u kiest u eigen trouwambtenaar. 
Kijk voor meer informatie op de pagina Trouwambtenaar.</li><li>
De trouwambtenaar houdt een toespraak. Daarbij mag ook een eigen toespraak.</li>
</ul>', detail_url = 'https://www.rotterdam.nl/trouwlocatie-en-ceremonie', vrije_locatie = false WHERE id = 8;
UPDATE public.trouwlocaties SET naam = 'Trouwzaal 2', foto_url = '/trouwlocaties/trouwzaal2.webp', aangemaakt_op = '2026-11-03 14:49:16.226682', omschrijving = e'<ul><li>De trouwzaal is op de begane grond van het stadhuis.
In de trouwzaal is plaats voor 50 personen.</li><li>
De ceremonie duurt 30 minuten. Dit is inclusief inloop en uitloop, het officiële deel, de toespraak en de felicitaties na afloop in de felicitatiekamer.</li><li>
U hebt minimaal 2 getuigen, maximaal 4.</li><li>
U krijgt 2 parkeerkaarten. Voor de andere gasten zijn er genoeg parkeermogelijkheden in de buurt. Dat is wel betaald parkeren.</li><li>
U mag muziek meenemen en afspelen via cd, mp3 of bluetooth.</li><li>
U kiest uw top 3 van trouwambtenaren van de gemeente. Of u kiest u eigen trouwambtenaar. 
Kijk voor meer informatie op de pagina Trouwambtenaar.</li><li>
De trouwambtenaar houdt een toespraak. Daarbij mag ook een eigen toespraak. </li>
<ul>', detail_url = 'https://www.rotterdam.nl/trouwlocatie-en-ceremonie', vrije_locatie = false WHERE id = 9;
UPDATE public.trouwlocaties SET naam = 'Kies uw eigen locatie', foto_url = null, aangemaakt_op = '2026-11-10 13:38:56.089855', omschrijving = e'<h3>U mag ook trouwen of een partnerschap registreren op een locatie die niet van de gemeente is. Dit kan overal en op elk moment. Ook in het weekend en \'s nachts. Zolang de locatie maar veilig en voor iedereen toegankelijk is.</H3>
<ul>
<li>Uw ceremonie duurt 60 minuten.</li><li>Het aantal personen dat aanwezig mag zijn, verschilt per locatie.</li><li>
U hebt minimaal 2 en maximaal 4 getuigen nodig.</li><li>
Parkeren verschilt per locatie. Vraag dit na bij de locatie.</li><li>
Of u muziek kunt afspreken, verschilt per locatie.</li><li>
U kiest uw top 3 trouwambtenaren van de gemeente. U kunt ook uw eigen trouwambtenaar kiezen. Kijk op de pagina Trouwambtenaar voor meer informatie.</li><li>
De trouwambtenaar houdt een toespraak. U mag ook zelf een toespraak houden.</li>
</ul>
<p>
De gemeentelijke kosten hangen af van de datum en het tijdstip. U ziet de kosten bij uw gekozen datum en tijd.
</p><p>
U betaalt tussen de 770,90 en  2.168,60 euro afhankelijk van de datum en tijd. De huur van de locatie betaalt u apart.
</p><p>
U heeft hiervoor een locatieverklaring nodig</p>', detail_url = null, vrije_locatie = true WHERE id = 12;

UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 11, huwelijkstype = 'GRATIS', dag_van_de_week = 'MONDAY', start_tijd = '09:00:00', eind_tijd = '11:50:00', duur_in_minuten = 10, prijs = 0.00, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 120;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 11, huwelijkstype = 'EENVOUDIG', dag_van_de_week = 'TUESDAY', start_tijd = '10:15:00', eind_tijd = '12:15:00', duur_in_minuten = 15, prijs = 216.60, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 121;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 11, huwelijkstype = 'EENVOUDIG', dag_van_de_week = 'WEDNESDAY', start_tijd = '10:15:00', eind_tijd = '12:15:00', duur_in_minuten = 15, prijs = 216.60, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 122;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 8, huwelijkstype = 'REGULIER', dag_van_de_week = 'MONDAY', start_tijd = '10:00:00', eind_tijd = '11:30:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 123;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 8, huwelijkstype = 'REGULIER', dag_van_de_week = 'TUESDAY', start_tijd = '10:00:00', eind_tijd = '11:30:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 124;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 8, huwelijkstype = 'REGULIER', dag_van_de_week = 'WEDNESDAY', start_tijd = '10:00:00', eind_tijd = '11:30:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 125;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 8, huwelijkstype = 'REGULIER', dag_van_de_week = 'MONDAY', start_tijd = '14:00:00', eind_tijd = '16:00:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 126;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 8, huwelijkstype = 'REGULIER', dag_van_de_week = 'TUESDAY', start_tijd = '14:00:00', eind_tijd = '16:00:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 127;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 8, huwelijkstype = 'REGULIER', dag_van_de_week = 'WEDNESDAY', start_tijd = '14:00:00', eind_tijd = '16:00:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 128;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 8, huwelijkstype = 'REGULIER', dag_van_de_week = 'THURSDAY', start_tijd = '09:00:00', eind_tijd = '12:00:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 129;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 8, huwelijkstype = 'REGULIER', dag_van_de_week = 'FRIDAY', start_tijd = '09:00:00', eind_tijd = '12:00:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 130;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 8, huwelijkstype = 'REGULIER', dag_van_de_week = 'THURSDAY', start_tijd = '13:30:00', eind_tijd = '16:00:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 131;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 8, huwelijkstype = 'REGULIER', dag_van_de_week = 'FRIDAY', start_tijd = '13:30:00', eind_tijd = '16:00:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 132;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 9, huwelijkstype = 'REGULIER', dag_van_de_week = 'MONDAY', start_tijd = '14:15:00', eind_tijd = '16:15:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 133;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 9, huwelijkstype = 'REGULIER', dag_van_de_week = 'TUESDAY', start_tijd = '14:15:00', eind_tijd = '16:15:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 134;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 9, huwelijkstype = 'REGULIER', dag_van_de_week = 'WEDNESDAY', start_tijd = '14:15:00', eind_tijd = '16:15:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 135;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 9, huwelijkstype = 'REGULIER', dag_van_de_week = 'THURSDAY', start_tijd = '09:15:00', eind_tijd = '11:45:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 136;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 9, huwelijkstype = 'REGULIER', dag_van_de_week = 'FRIDAY', start_tijd = '09:15:00', eind_tijd = '11:45:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 137;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 9, huwelijkstype = 'REGULIER', dag_van_de_week = 'THURSDAY', start_tijd = '13:45:00', eind_tijd = '16:15:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 138;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 9, huwelijkstype = 'REGULIER', dag_van_de_week = 'FRIDAY', start_tijd = '13:45:00', eind_tijd = '16:15:00', duur_in_minuten = 30, prijs = 624.40, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 139;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 10, huwelijkstype = 'REGULIER', dag_van_de_week = 'MONDAY', start_tijd = '09:00:00', eind_tijd = '12:00:00', duur_in_minuten = 30, prijs = 981.50, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 140;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 10, huwelijkstype = 'REGULIER', dag_van_de_week = 'MONDAY', start_tijd = '14:00:00', eind_tijd = '16:00:00', duur_in_minuten = 30, prijs = 981.50, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 141;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 10, huwelijkstype = 'REGULIER', dag_van_de_week = 'TUESDAY', start_tijd = '09:00:00', eind_tijd = '12:00:00', duur_in_minuten = 30, prijs = 981.50, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 142;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 10, huwelijkstype = 'REGULIER', dag_van_de_week = 'TUESDAY', start_tijd = '14:00:00', eind_tijd = '16:00:00', duur_in_minuten = 30, prijs = 981.50, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 143;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 10, huwelijkstype = 'REGULIER', dag_van_de_week = 'WEDNESDAY', start_tijd = '09:00:00', eind_tijd = '12:00:00', duur_in_minuten = 30, prijs = 981.50, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 144;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 10, huwelijkstype = 'REGULIER', dag_van_de_week = 'WEDNESDAY', start_tijd = '14:00:00', eind_tijd = '16:00:00', duur_in_minuten = 30, prijs = 981.50, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 145;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 10, huwelijkstype = 'REGULIER', dag_van_de_week = 'THURSDAY', start_tijd = '09:00:00', eind_tijd = '12:00:00', duur_in_minuten = 30, prijs = 981.50, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 146;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 10, huwelijkstype = 'REGULIER', dag_van_de_week = 'THURSDAY', start_tijd = '14:00:00', eind_tijd = '16:00:00', duur_in_minuten = 30, prijs = 981.50, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 147;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 10, huwelijkstype = 'REGULIER', dag_van_de_week = 'FRIDAY', start_tijd = '09:00:00', eind_tijd = '12:00:00', duur_in_minuten = 30, prijs = 981.50, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 148;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 10, huwelijkstype = 'REGULIER', dag_van_de_week = 'FRIDAY', start_tijd = '14:00:00', eind_tijd = '16:00:00', duur_in_minuten = 30, prijs = 981.50, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 149;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 10, huwelijkstype = 'REGULIER', dag_van_de_week = 'SATURDAY', start_tijd = '13:45:00', eind_tijd = '14:00:00', duur_in_minuten = 30, prijs = 1429.10, ingangsdatum = '2026-01-01', einddatum = '2026-12-31' WHERE id = 150;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'MONDAY', start_tijd = '00:00:00', eind_tijd = '09:00:00', duur_in_minuten = 60, prijs = 2168.60, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 151;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'MONDAY', start_tijd = '09:00:00', eind_tijd = '17:00:00', duur_in_minuten = 60, prijs = 770.90, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 152;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'MONDAY', start_tijd = '17:00:00', eind_tijd = '21:00:00', duur_in_minuten = 60, prijs = 1084.30, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 153;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'MONDAY', start_tijd = '21:00:00', eind_tijd = '23:59:00', duur_in_minuten = 60, prijs = 2168.60, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 154;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'TUESDAY', start_tijd = '00:00:00', eind_tijd = '09:00:00', duur_in_minuten = 60, prijs = 2168.60, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 155;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'TUESDAY', start_tijd = '09:00:00', eind_tijd = '17:00:00', duur_in_minuten = 60, prijs = 770.90, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 156;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'TUESDAY', start_tijd = '17:00:00', eind_tijd = '21:00:00', duur_in_minuten = 60, prijs = 1084.30, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 157;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'TUESDAY', start_tijd = '21:00:00', eind_tijd = '23:59:00', duur_in_minuten = 60, prijs = 2168.60, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 158;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'WEDNESDAY', start_tijd = '00:00:00', eind_tijd = '09:00:00', duur_in_minuten = 60, prijs = 2168.60, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 159;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'WEDNESDAY', start_tijd = '09:00:00', eind_tijd = '17:00:00', duur_in_minuten = 60, prijs = 770.90, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 160;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'WEDNESDAY', start_tijd = '17:00:00', eind_tijd = '21:00:00', duur_in_minuten = 60, prijs = 1084.30, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 161;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'WEDNESDAY', start_tijd = '21:00:00', eind_tijd = '23:59:00', duur_in_minuten = 60, prijs = 2168.60, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 162;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'THURSDAY', start_tijd = '00:00:00', eind_tijd = '09:00:00', duur_in_minuten = 60, prijs = 2168.60, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 163;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'THURSDAY', start_tijd = '09:00:00', eind_tijd = '17:00:00', duur_in_minuten = 60, prijs = 770.90, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 164;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'THURSDAY', start_tijd = '17:00:00', eind_tijd = '21:00:00', duur_in_minuten = 60, prijs = 1084.30, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 165;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'THURSDAY', start_tijd = '21:00:00', eind_tijd = '23:59:00', duur_in_minuten = 60, prijs = 2168.60, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 166;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'FRIDAY', start_tijd = '00:00:00', eind_tijd = '09:00:00', duur_in_minuten = 60, prijs = 2168.60, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 167;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'FRIDAY', start_tijd = '09:00:00', eind_tijd = '17:00:00', duur_in_minuten = 60, prijs = 770.90, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 168;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'FRIDAY', start_tijd = '17:00:00', eind_tijd = '21:00:00', duur_in_minuten = 60, prijs = 1084.30, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 169;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'FRIDAY', start_tijd = '21:00:00', eind_tijd = '23:59:00', duur_in_minuten = 60, prijs = 2168.60, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 170;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'SATURDAY', start_tijd = '00:00:00', eind_tijd = '09:00:00', duur_in_minuten = 60, prijs = 2168.60, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 171;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'SATURDAY', start_tijd = '09:00:00', eind_tijd = '21:00:00', duur_in_minuten = 60, prijs = 1084.30, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 172;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'SATURDAY', start_tijd = '21:00:00', eind_tijd = '23:59:00', duur_in_minuten = 60, prijs = 2168.60, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 173;
UPDATE public.trouwlocaties_beschikbaarheden SET locatie_id = 12, huwelijkstype = 'REGULIER', dag_van_de_week = 'SUNDAY', start_tijd = '00:00:00', eind_tijd = '23:59:00', duur_in_minuten = 60, prijs = 2168.60, ingangsdatum = '2016-01-01', einddatum = '2026-12-31' WHERE id = 174;
