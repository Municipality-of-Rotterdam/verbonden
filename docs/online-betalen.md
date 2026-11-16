# Online betalen

Dit document beschrijft hoe online betalen in Verbonden werkt, vooral op functioneel niveau. Het is bedoeld
voor gemeenten die willen beoordelen of Verbonden bij hen past: wat ziet de burger, wat ziet de medewerker, en
wat moet een gemeente zelf regelen.

## In het kort

- Het paar betaalt online bij het indienen van de aanvraag. **Pas als de betaling gelukt is, is de aanvraag
  ingediend.** Een aanvraag die niets kost, wordt meteen ingediend.
- Komen er na het indienen nog kosten bij (bijvoorbeeld een duurder trouwboekje of alsnog een internationale
  akte), dan betaalt het paar die apart, ook online.
- Beide partners kunnen betalen: degene die op dat moment is ingelogd met DigiD.
- Verbonden betaalt niet zelf. De gemeente koppelt een **betaalprovider** (bijvoorbeeld een eigen
  betaalmodule of een PSP met iDEAL). Verbonden bevat een duidelijk koppelvlak daarvoor, en een mock om lokaal
  mee te testen.
- Terugbetalen (crediteren) gebeurt buiten Verbonden.

## Wat de burger ziet

### Betalen bij het indienen

1. Het paar vult de aanvraag in en komt op de pagina **Controleren**. Daar staan alle keuzes en het totaalbedrag.
2. De knop heet **Bevestigen en betalen**. Kost de aanvraag niets, dan heet hij **Bevestigen en indienen**.
3. Na de klik gaat de burger meteen naar de betaalpagina van de betaalprovider. Er is geen tussenpagina.
4. Na het betalen komt de burger terug in Verbonden. Wat er dan gebeurt, hangt af van de uitslag:

| Uitslag | Wat de burger ziet |
|---|---|
| Gelukt | De statuspagina: "Uw aanvraag is ontvangen". De aanvraag is ingediend. |
| Niet gelukt of afgebroken | De pagina Controleren, met de melding dat de betaling niet is gelukt. De burger kan het opnieuw proberen. |
| Nog niet bekend | De pagina "Uw betaling is in behandeling". Banken bevestigen een betaling soms pas na een paar minuten. De pagina gaat vanzelf verder zodra de uitslag binnen is. Duurt het lang, dan legt de pagina uit dat de burger de pagina mag sluiten en later opnieuw kan inloggen. |

Zolang een betaling in behandeling is, kan niemand nog een keer betalen: ook de andere partner niet. Zo wordt
er niet dubbel betaald.

### De statuspagina na het indienen

Op de statuspagina staat onder "Wat gebeurt er nu?" een stap **Betaling**, direct na "Aanvraag ingediend". Die
stap laat zien wat er betaald is en wat er nog openstaat:

| Situatie | Stap |
|---|---|
| Alles betaald | **Betaling gelukt** — U heeft € 277,80 betaald. |
| Er staat nog iets open | **Nog te betalen** — U heeft € 260,00 betaald. Er staat nog € 17,80 open. |
| De bank moet nog bevestigen | **Betaling in behandeling** — U heeft € 260,00 betaald. Uw bank moet de betaling van € 17,80 nog bevestigen. |
| Laatste poging mislukt | **Betaling niet gelukt** — U heeft € 260,00 betaald. De betaling van € 17,80 is niet gelukt. U kunt het opnieuw proberen. |

Is er nooit iets betaald en staat er niets open (een gratis aanvraag, of een aanvraag die aan de balie is
afgerekend), dan staat deze stap er niet.

### Later nog iets betalen

Staat er na het indienen nog een bedrag open, dan ziet het paar op de statuspagina een blok **Te betalen**, met
per bedrag een omschrijving en een knop **Betalen**. Na het betalen werkt het net als hierboven: gelukt,
niet gelukt of in behandeling.

## Wat de medewerker ziet

### Betalingen

In beheer staat het menu-item **Betalingen**: een overzicht van alle betaalpogingen, nieuwste eerst. Per
poging staan:

- het dossier (met een link ernaartoe);
- waarvoor betaald is ("Aanvraag" of de omschrijving van een betaalmutatie);
- het bedrag en de status: wacht op uitslag, gelukt, of mislukt/afgebroken;
- het betalingskenmerk en het kenmerk bij de betaalprovider.

Een medewerker kan zoeken op het betalingskenmerk, op het kenmerk bij de betaalprovider of op het dossier-ID.
Zo vindt hij een betaling die hij bij de betaalprovider of op een bankafschrift ziet snel terug, met het
dossier erbij. Het betalingskenmerk staat ook in de omschrijving die de burger bij de betaalprovider en op het
bankafschrift ziet; dat is handig als een burger belt.

### Dossierdetails

Bij elk dossier staan twee nieuwe onderdelen:

- **Betalingen**: alle betaalpogingen van dit dossier, met wie er betaald heeft.
- **Betaalmutaties**: bedragen die na het indienen nog betaald moeten worden of al betaald zijn.

### Betaalmutaties

Een betaalmutatie is een bedrag dat het paar na het indienen nog moet betalen. Er zijn twee soorten:

- **Automatisch (prijswijziging).** Wijzigt een medewerker de extra's of de ceremonie van een ingediend
  dossier, en wordt het dossier daardoor duurder? Dan maakt Verbonden zelf een betaalmutatie aan voor het
  verschil. Voorbeeld: het paar heeft € 260 betaald en vraagt aan de balie alsnog een internationale akte aan.
  De medewerker vinkt de akte aan; het paar ziet daarna "Te betalen: Internationale huwelijksakte € 17,80".
- **Handmatig.** Voor andere gevallen voegt de medewerker zelf een betaalmutatie toe, met een omschrijving en
  een bedrag.

Een betaalmutatie kan weer weg, zolang het paar er nog geen betaling voor heeft gestart.

Wordt een dossier na het indienen goedkoper (bijvoorbeeld een extra geschrapt), dan maakt Verbonden geen
negatief bedrag aan. De dossierdetails tonen dan een waarschuwing: "Er is € … meer betaald of te betalen dan
het dossier nu kost. Terugbetalen gaat buiten Verbonden om."

## Hoe Verbonden zorgt dat het klopt

- **De uitslag komt van de betaalprovider, niet van de browser.** Wat de browser na het betalen meestuurt,
  gebruikt Verbonden niet: dat is niet te vertrouwen en de browser kan onderweg crashen. De betaalprovider meldt
  de uitslag rechtstreeks aan Verbonden (server naar server). Daardoor wordt een aanvraag ook ingediend als de
  burger het tabblad sluit tijdens het betalen.
- **Een melding mag vaker komen.** Dezelfde uitslag nog eens verwerken verandert niets.
- **Een melding moet bij de betaling passen.** Stuurt de betaalprovider een eigen kenmerk of het bedrag mee, dan
  controleert Verbonden die. Klopt het niet, dan wordt de melding genegeerd en gelogd.
- **Elke betaalpoging heeft een eigen, uniek kenmerk** van 16 cijfers. Cijfers, omdat niet elke betaalprovider
  letters accepteert.
- **Geen dubbele betaling** zolang er een poging op de uitslag wacht.

## Wat een gemeente zelf moet regelen

### Een koppeling met de betaalprovider

Verbonden is opgezet als een kern met koppelvlakken (de zogenaamde SPI) waarop een gemeente haar eigen
voorzieningen aansluit, net als bij DigiD en persoonsgegevens. Voor online betalen is dat het package
`online_betalen` in `core`:

- **`OnlineBetaalService`**: de gemeente implementeert dit. Verbonden geeft een opdracht met onder meer het
  betalingskenmerk, het bedrag, de betaler en de URL waar de burger na het betalen terugkomt. De implementatie
  maakt de betaling aan bij de betaalprovider en geeft de URL van de betaalpagina terug.
- **`BetaalUitslagVerwerker`**: Verbonden implementeert dit. De koppeling roept het aan zodra de betaalprovider
  de uitslag meldt (gelukt of mislukt).

Wat de betaalprovider dus moet kunnen:

1. een betaling aanmaken voor een bedrag en een kenmerk, en een betaalpagina teruggeven;
2. de burger na het betalen terugsturen naar een opgegeven URL;
3. de definitieve uitslag los van de browser melden (een callback of webhook), met het kenmerk erbij.

Voor lokaal ontwikkelen en testen zit er een mock-betaalprovider in de module `remote-local`. Die toont een
testpagina waarop je kiest: betaling gelukt, mislukt, uitslag na 10 seconden, of een langzame bank (na 3
minuten).

### Inrichting en beheer

- **Een account bij een betaalprovider**, met de rekening waarop het geld binnenkomt. De boeking in de
  financiële administratie (grootboek, kostendrager) regelt de gemeente via die rekening.
- **Afscherming van het callback-adres.** De betaalprovider meldt de uitslag zonder in te loggen. Zorg op
  netwerkniveau dat alleen de betaalprovider dat adres kan bereiken.
- **Monitoring.** Situaties die handwerk vragen (zie hieronder) logt Verbonden als fout. Zet daar een melding op.
- **Een werkinstructie voor de backoffice** over betaalmutaties, de waarschuwing bij te veel betaald, en het
  terugvinden van betalingen.

## Wat Verbonden (nog) niet doet

- **Terugbetalen of crediteren.** Dat gaat buiten Verbonden om.
- **Een factuur sturen.** Betalen gebeurt online, vooraf.
- **Een bevestiging per e-mail na het betalen.** Die komt nog.
- **Een aanvraag in delen betalen.** Het paar betaalt het totaalbedrag in één keer; latere wijzigingen zijn
  aparte betaalmutaties.

## Handwerk voor de backoffice

Deze situaties komen zelden voor, maar Verbonden lost ze niet zelf op. Ze staan als fout in de log en zijn
terug te vinden in het betalingenoverzicht:

- **Dubbel betaald**: bijvoorbeeld als beide partners op precies hetzelfde moment betalen.
- **Betaald, maar het dossier kon niet worden ingediend**, omdat het intussen niet meer compleet was.
- **Een uitslag voor een onbekend kenmerk**, of een melding die niet bij de betaling past.
- **De bank meldt nooit een uitslag.** De betaling blijft dan "in behandeling" tot de betaalprovider zelf
  "niet betaald" meldt; daarna kan het paar opnieuw betalen.

## Begrippen

| Begrip | Betekenis |
|---|---|
| Betaalprovider | De dienst die de betaling afhandelt, zoals een PSP met iDEAL of een eigen betaalmodule van de gemeente. |
| Betaalpoging | Eén keer betalen. Mislukt een poging, dan start een nieuwe poging met een nieuw kenmerk. |
| Betalingskenmerk | Het unieke kenmerk van een betaalpoging, 16 cijfers. Staat ook bij de betaalprovider en in de omschrijving op het bankafschrift. |
| Kenmerk bij de betaalprovider | Het kenmerk dat de betaalprovider zelf aan de betaling geeft. |
| Betaalmutatie | Een bedrag dat het paar na het indienen nog moet betalen, automatisch (prijswijziging) of handmatig toegevoegd. |
| In behandeling | De burger heeft betaald, maar de bank heeft de uitslag nog niet gemeld. |
