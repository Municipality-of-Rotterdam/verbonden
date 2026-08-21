# Bijdragen aan Verbonden

Allereerst bedankt dat je wilt bijdragen aan dit project! Zonder jouw input
wordt dit nooit een beter open source project.

## Code of Conduct

Dit project hanteert een [Code of Conduct](CODE_OF_CONDUCT.md). Door bij te
dragen aan dit project ga je akkoord met de voorwaarden hiervan.

## Hoe kan je bijdragen?

### Meld een bug

Heb je een bug gevonden? Maak dan een
[issue](https://github.com/Municipality-of-Rotterdam/verbonden/issues)
aan met:

- Een duidelijke en beschrijvende titel
- Stappen om de bug te reproduceren
- Verwacht gedrag vs. daadwerkelijk gedrag
- Screenshots of foutmeldingen (indien van toepassing)
- Je omgeving (OS, browserversie, etc.)

### Features voorstellen

Heb je een idee voor een nieuwe feature? Open een issue met:

- Een duidelijke beschrijving van de feature
- Waarom deze feature waardevol zou zijn
- Eventuele voorbeelden of mockups

### Documentatie verbeteren

Documentatie kan altijd beter! Pull requests voor verbeteringen aan de
documentatie zijn zeer welkom.

## Ontwikkelproces

### 1. Fork en clone de repository

```bash
git clone https://github.com/Municipality-of-Rotterdam/verbonden.git
cd verbonden
```

### 2. Maak een nieuwe branch

```bash
git checkout -b feature/mijn-nieuwe-feature
```

Of voor bugfixes:

```bash
git checkout -b fix/issue-nummer-korte-beschrijving
```

### 3. Zet de ontwikkelomgeving op

Volg de instructies in de [README](README.md#lokaal-opstarten) om de
database te starten en de applicatie lokaal te draaien.

### 4. Maak je wijzigingen

Volg de architectuurregels in [AGENTS.md](AGENTS.md) — deze beschrijven de
packagestructuur, service-layer conventies, Wicket-patronen en
integratietest-conventies die in dit project gehanteerd worden.

- Schrijf duidelijke, leesbare code
- Voeg tests toe voor nieuwe functionaliteit
- Update documentatie waar nodig

### 5. Test je wijzigingen

```bash
./mvnw verify
```

De tests gebruiken een embedded PostgreSQL 18 database en vereisen geen
draaiende Docker-container.

### 6. Commit je wijzigingen

We gebruiken [Conventional Commits](https://www.conventionalcommits.org/)
voor onze commit messages:

```
<type>(<scope>): <beschrijving>
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`.

### 7. Push naar je fork en open een Pull Request

- Geef je PR een duidelijke titel en beschrijving
- Link gerelateerde issues
- Zorg dat de CI-pipeline (`./mvnw verify`) slaagt
- Wacht op review van een maintainer

## Toegankelijkheid (Accessibility)

Voor Nederlandse overheidsprojecten is toegankelijkheid wettelijk verplicht:

- Volg [WCAG 2.1](https://www.w3.org/WAI/WCAG21/quickref/) niveau AA
- Test met screenreaders (NVDA/JAWS)
- Zorg voor keyboard navigatie
- Gebruik semantische HTML
- Test kleurcontrast (minimaal 4.5:1)

## Beveiliging (Security)

- Meld beveiligingsproblemen **NIET** via publieke issues
- Gebruik het proces beschreven in [SECURITY.md](SECURITY.md)
- Volg [OWASP Top 10](https://owasp.org/www-project-top-ten/) best practices
- Houd rekening met de
  [BIO](https://www.digitaleoverheid.nl/overzicht-van-alle-onderwerpen/cybersecurity/bio-en-ensia/baseline-informatiebeveiliging-overheid/)
  normen

## Licentie

Door bij te dragen aan dit project ga je ermee akkoord dat je bijdragen
worden gelicenseerd onder de **EUPL-1.2** licentie, zodat de code ook door
andere overheidsorganisaties gebruikt mag worden.

---

Bedankt voor je bijdrage!

_Dit project wordt onderhouden door Gemeente Rotterdam._
