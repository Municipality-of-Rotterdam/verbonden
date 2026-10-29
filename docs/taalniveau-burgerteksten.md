# Taalniveau burgerteksten

Analyse van de teksten op de burgerpagina's met de skill `taalniveau` (`.claude/skills/taalniveau/SKILL.md`). Streefniveau: **B1**. Aanspreekvorm: **u/uw** (zie `AGENTS.md`).

Bijna alle teksten staan in `core/src/main/java/nl/rotterdam/verbonden/core/WicketApplication.properties`. De tekst in de HTML-bestanden is voorbeeldtekst; die wordt bij het tonen vervangen door de tekst uit het properties-bestand. Bij elk punt staat daarom de property-sleutel.

Beheerpagina's zijn niet beoordeeld. Die zijn voor medewerkers; daar is vaktaal toegestaan (C1).

## Taalniveau: B1, met uitschieters naar B2

**Motivering:**
- Gemiddelde zinslengte: 7,7 woorden (150 zinnen). Dat past bij B1, maar het zijn vooral korte knop- en kopteksten. Drie zinnen zijn langer dan 20 woorden.
- Vakjargon: dossier, koppelen, gemachtigd, omgeleid, URL, verifiëren, uittreksel, mits, privévenster, verklaring naamgebruik.
- Passieve zinnen: "wordt in behandeling genomen", "kan niet meer worden gewijzigd", "wordt uw partner gevraagd".
- De aanspreekvorm is gelijkgetrokken naar u/uw. Eerder wisselden u, je en jullie elkaar af.

---

## Verbeterpunten

### Hoge impact

#### `intake.intro.marriageintake`
- **Origineel:** Later in dit proces wordt uw partner gevraagd om in te loggen via DigiD om te bevestigen dat deze registratie klopt.
- **B1-versie:** Daarna vragen we uw partner om in te loggen met DigiD. Zo bevestigt uw partner dat de gegevens kloppen.
- **Waarom beter:** De zin van 21 woorden met een passieve vorm en twee keer "om … te" wordt twee actieve zinnen.

#### `intake.alert.wrong.dossier`
- **Origineel:** De ingevoerde URL verwijst naar een ander dossier. U bent automatisch omgeleid naar uw eigen dossier.
- **B1-versie:** Deze link hoort bij een andere aanvraag. Daarom ziet u nu uw eigen aanvraag.
- **Waarom beter:** "URL", "verwijst" en "omgeleid" zijn technische woorden.

#### `intake.alert.not.authorized`
- **Origineel:** U bent niet gemachtigd om het opgevraagde dossier in te zien. U kunt hier een nieuw dossier aanmaken.
- **B1-versie:** U mag deze aanvraag niet bekijken. U kunt hier wel een nieuwe aanvraag starten.
- **Waarom beter:** "Gemachtigd", "opgevraagde" en "inzien" zijn ambtelijke woorden.

#### `intake.alert.invitation.blocked`
- **Origineel:** U bent uitgenodigd voor een ander dossier, maar u heeft al een lopende aanvraag. Daarom kunt u deze uitnodiging niet accepteren. Wilt u toch op de uitnodiging ingaan? Neem dan contact op met de gemeente om uw huidige aanvraag te laten annuleren.
- **B1-versie:** U heeft al een aanvraag die nog loopt. Daarom kunt u deze uitnodiging niet accepteren. Wilt u toch meedoen? Neem dan contact op met de gemeente. Die kan uw huidige aanvraag stoppen.
- **Waarom beter:** Kortere zinnen, en "ingaan op" en "laten annuleren" vervallen.

#### `de.getuigen.intro`
- **Origineel:** Geef hieronder de namen van uw getuigen door. Na betaling en bevestiging vragen wij om de gegevens te verifiëren door een identiteitscontrole. Dit vindt plaats in MijnLoket.
- **B1-versie:** Geef hieronder de namen van uw getuigen door. Na betaling en bevestiging controleren we wie uw getuigen zijn. Dat regelt u in MijnLoket.
- **Waarom beter:** Haalt "verifiëren", de naamwoordstijl ("een identiteitscontrole") en "vindt plaats" weg.

#### `extras.muziek.intro`
- **Origineel:** Wij hebben een vaste lijst van muziek waar u een keuze uit kunt maken. Deze kunt u na bevestiging door een lijst te downloaden uit MijnLoket. Er zijn een 5-tal momenten dat er muziek gespeeld kan worden tijdens de ceremonie.
- **B1-versie:** U kiest muziek uit een vaste lijst. Die lijst staat na de bevestiging in MijnLoket. Tijdens de ceremonie kan op 5 momenten muziek klinken.
- **Waarom beter:** De tweede zin klopt grammaticaal niet, en "5-tal" is stijf.
- **Let op (inhoud):** de tekst noemt 5 momenten, maar er zijn er 6 (`extras.muziek.moment1` t/m `moment6`), en drie heten nog "Reden 2", "Reden 3" en "Reden 4".

### Gemiddelde impact

#### `intake.registratie.description`
- **Origineel:** Wilt u weten wat het verschil is tussen een huwelijk en geregistreerd partnerschap, kijk dan even op onze pagina op rotterdam.nl/registratie
- **B1-versie:** Wat is het verschil tussen een huwelijk en een geregistreerd partnerschap? Lees het op rotterdam.nl/registratie.
- **Waarom beter:** De zin van 21 woorden wordt een korte vraag met een duidelijke verwijzing.

#### `extras.internationale.akte.intro`
- **Origineel:** … Kies dan voor een internationaal uittreksel. Dat is een samenvatting van uw huwelijksakte met uitleg in 10 talen: Nederlands, Engels, …
- **B1-versie:** … Kies dan voor een internationaal uittreksel. Dat is een korte versie van uw huwelijksakte. Er staat uitleg bij in 10 talen: Nederlands, Engels, …
- **Waarom beter:** De zin van 23 woorden wordt gesplitst, en "samenvatting" wordt "korte versie".

#### `aanvraag.status.INGEDIEND.tekst`
- **Origineel:** De huwelijksaanvraag is ontvangen en wordt in behandeling genomen.
- **B1-versie:** We hebben uw aanvraag ontvangen. De gemeente gaat die nu bekijken.
- **Waarom beter:** Actieve zinnen in plaats van passieve.

#### `aanvraag.status.wijzigen`
- **Origineel:** Uw aanvraag kan niet meer online worden gewijzigd. Klopt er iets niet? Neem dan contact op met de gemeente Rotterdam.
- **B1-versie:** U kunt de aanvraag niet meer online wijzigen. Klopt er iets niet? Neem dan contact op met de gemeente Rotterdam.
- **Waarom beter:** Actief in plaats van passief.

#### `intake.intro.dedag`
- **Origineel:** … Een datum heeft invloed op de agenda van een trouwambtenaar.
- **B1-versie:** … De datum bepaalt welke trouwambtenaren er kunnen.
- **Waarom beter:** De oorspronkelijke zin zegt niet duidelijk wat de lezer ermee moet.

#### `intake.sidebar.ontbrekend.PARTNER_GEKOPPELD` en `intake.sidebar.partner.nietgekoppeld`
- **Origineel:** Laat uw partner het dossier koppelen / Uw partner heeft het dossier nog niet gekoppeld.
- **B1-versie:** Uw partner moet nog inloggen en meedoen / Uw partner doet nog niet mee.
- **Waarom beter:** "Dossier koppelen" is systeemtaal.

#### `jullie.gegevens.partner.uitnodigen.instructie`
- **Origineel:** Uw partner moet zelf inloggen met DigiD om het dossier te koppelen. Dat kan niet in dit browservenster, omdat u hier al bent ingelogd.
- **B1-versie:** Uw partner moet zelf inloggen met DigiD. Dat kan niet in dit scherm, want u bent hier al ingelogd.
- **Waarom beter:** "Dossier koppelen" en "browservenster" vervallen.

#### `jullie.gegevens.partner.uitnodigen.link.uitleg`
- **Origineel:** Kopieer de link en stuur deze naar uw partner. Of open de link in een privévenster of een andere browser en laat uw partner daar inloggen.
- **B1-versie:** Kopieer de link en stuur deze naar uw partner. Of open de link in een andere browser. Laat uw partner daar inloggen.
- **Waarom beter:** "Privévenster" vervalt en de lange zin wordt gesplitst.

#### `intake.uitnodiging.eigen.concept`
- **Origineel:** U bent al begonnen met een eigen aanvraag. Doet u mee met deze uitnodiging, dan vervalt uw eigen aanvraag. Uw keuzes daarin worden niet overgenomen.
- **B1-versie:** U bent al begonnen met een eigen aanvraag. Doet u mee met deze uitnodiging? Dan stopt uw eigen aanvraag. Uw keuzes daaruit nemen we niet mee.
- **Waarom beter:** "Vervalt" is formeel en "worden niet overgenomen" is passief.

### Lage impact

#### `intake.soort.*.beschrijving.*`
- **Origineel:** Datum kunt u kiezen (mits beschikbaar) / Geen voorkeur opgeven trouwambtenaar
- **B1-versie:** U kiest zelf de datum (als die vrij is) / U kunt geen trouwambtenaar kiezen
- **Waarom beter:** "Mits" is B2-woordenschat, en de telegramstijl is lastig te lezen.

#### `extras.ringen.intro`
- **Origineel:** Wilt u een moment om ringen bij elkaar om te doen?
- **B1-versie:** Wilt u elkaar tijdens de ceremonie een ring omdoen?
- **Waarom beter:** Korter en directer.

#### Kleine punten
| Sleutel | Origineel | B1-versie |
|---|---|---|
| `de.getuigen.heading` | Benoem uw getuigen | Kies uw getuigen |
| `intake.uitnodiging.accepteer` | Ja, Ik wil! | Ja, ik wil! |
| `jullie.gegevens.heading` | Uw gegevens & verklaring naamgebruik | Uw gegevens en achternaam |
| `intake.soort.description` | Al uw keuzes hebben invloed op elkaar. | Uw keuzes hangen met elkaar samen. |

---

## Samenvatting
- **Belangrijkste knelpunt:** de foutmeldingen en uitleg in systeemtaal (dossier, koppelen, gemachtigd, URL, verifiëren).
- **Snelste winst:** de drie meldingen over toegang tot een dossier (`intake.alert.*`) en de getuigen-intro herschrijven.
- **Geschat verbetertraject:** klein. Bijna alles staat in één properties-bestand.

## Open punten
- **Inhoud muziekmomenten:** het aantal momenten (5 of 6) en de namen "Reden 2–4".
- **Inclusiviteit:** "Opkomst bruid" en "Verlaten zaal door bruidspaar" passen niet altijd, bijvoorbeeld bij een geregistreerd partnerschap of bij twee partners van hetzelfde geslacht.
