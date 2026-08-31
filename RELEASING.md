# Releasen naar Maven Central

Deze repository publiceert naar Maven Central onder de `nl.rotterdam`
Sonatype-namespace, via het [Central Portal](https://central.sonatype.com/).

## Wat wordt gepubliceerd

- `nl.rotterdam.verbonden:verbonden` — de parent-POM van de reactor. Wordt
  gepubliceerd omdat `verbonden-core` en `verbonden-remote-local` hiervan
  overerven, en consumers die deze artefacten resolven ook de parent-POM
  moeten kunnen ophalen.
- `nl.rotterdam.verbonden:verbonden-core` — de gedeelde productiecode en het
  publieke SPI-contract.
- `nl.rotterdam.verbonden:verbonden-remote-local` — de mock-adapter /
  referentie-implementatie.

`verbonden-app-local` wordt **niet** gepubliceerd — dat is het uitvoerbare
lokale Spring Boot-artefact, geen library waar andere projecten van
afhankelijk zijn.

Deze repository heeft zijn eigen credentials en deelt die niet met andere
Rotterdam-repositories, ook al mogen die repositories dezelfde gedeelde
`nl.rotterdam`-namespace in Sonatype beheren.

## Status

### Gedaan (in code, op branch `release-to-maven-central`)

- POM-metadata (`url`, `developers`, `scm`) toegevoegd aan de root-, `core`-
  en `remote-local`-POM's — vereist door Central.
- Een Maven-profiel `release` toegevoegd dat `central-publishing-maven-plugin`,
  `maven-gpg-plugin`, `maven-source-plugin` en `maven-javadoc-plugin`
  activeert. Dit profiel is alleen actief met `-Prelease`, zodat de gewone
  `mvnw verify` (o.a. gebruikt in `.github/workflows/ci.yml`) ongewijzigd
  blijft werken — er zijn daar geen GPG/Sonatype-secrets beschikbaar.
- `app-local` expliciet uitgesloten van publicatie via `maven.deploy.skip=true`.
  Het `release`-profiel zelf staat eenmalig in de parent-POM en erft door naar
  alle modules (ook `app-local`) — tijdens `-Prelease` wordt `app-local` dus
  wel gesigneerd en van sources-/javadoc-jars voorzien, maar de deploy-stap
  slaat 'm over. Gecontroleerd via `mvn help:effective-pom -Prelease -pl
  app-local` dat `maven.deploy.skip` daar staat.
- `.github/workflows/release.yml` toegevoegd: handmatige
  `workflow_dispatch`-workflow met een versie-input, die de versie zet,
  bouwt/test, deployt naar Central (`-Prelease`) en de versiebump + tag
  commit en pusht.
- Lokaal geverifieerd: `mvnw verify` slaagt ongewijzigd (geen signing-poging),
  en `-Prelease` activeert de juiste plugins voor root/`core`/`remote-local`
  maar niet voor `app-local`.
- Dit document (`RELEASING.md`).

### Nog te doen (handmatig, buiten deze code om)

1. Een toegewijde GPG-sleutel genereren voor dit repository (niet de NLDS-sleutel
   hergebruiken).
2. De publieke sleutel publiceren naar een keyserver.
3. Een Sonatype Central Portal user token genereren voor dit repository/team
   (niet het NLDS-token hergebruiken).
4. Bevestigen/aanvragen dat de gebruikte Sonatype-account lid is van de
   `nl.rotterdam`-namespace.
5. De 4 secrets toevoegen aan het GitHub **environment** `release` op
   `Municipality-of-Rotterdam/verbonden` (niet repo- of org-brede secrets):
   `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`, `MAVEN_CENTRAL_USERNAME`,
   `MAVEN_CENTRAL_PASSWORD`. De workflow (`release.yml`) declareert
   `environment: release` op de job, zodat alleen die job er toegang toe
   heeft.
6. De POM-wijzigingen en de nieuwe workflow/documentatie committen en pushen
   (staan nu alleen lokaal op de branch `release-to-maven-central`) en een PR
   openen.
7. Een test-release doen (bijv. versie `0.0.1-rc1`) om de volledige keten
   te verifiëren voordat dit als routine wordt behandeld.
8. Optioneel: het teamcontact in de POM's (`Gemeente Rotterdam - Mijnloket
   ontwikkelteam`, hergebruikt van `publiccode.yml`/README) laten bevestigen
   door het team, of vervangen door een specifieker contact voor
   Maven Central-publicaties.

## Eenmalige configuratie (checklist bij het uitvoeren van punt 1 t/m 5 hierboven)

1. Genereer een GPG-sleutelpaar met een rol-/teamidentiteit (niet een
   persoonlijk account), zodat de sleutel personeelswisselingen overleeft:
   ```
   gpg --full-generate-key
   ```
   Gekozen antwoorden op de prompts:
   - **Kind of key**: `(10) ECC (sign only)` — er is alleen een signing-key
     nodig voor Maven Central, geen encryptie.
   - **Elliptic curve**: `(1) Curve 25519` (Ed25519) — de default, breed
     ondersteund door GnuPG en Sonatype's Central Portal.
   - **Validity**: `2y` — niet "does not expire" (slechte practice voor een
     sleutel die in CI-secrets leeft), maar ook niet zo kort dat rotatie
     hinderlijk wordt. Zet een reminder om de sleutel op tijd te vernieuwen;
     zie [Credentials roteren](#credentials-roteren).
   - **Real name**: `Gemeente Rotterdam - Mijnloket ontwikkelteam` (niet de
     naam van de persoon die de sleutel genereert) — consistent met de
     `<developers>`-entry in de parent-POM, en zodat de sleutel niet aan één
     teamlid gebonden is.
   - **Email**: `mijnloket_ontwikkelteam@Rotterdam.onmicrosoft.com` —
     hetzelfde teamadres als in de `<developers>`-entry.
   - **Passphrase**: gegenereerd/gekozen wachtwoord, bewaard in de
     teamwachtwoordmanager (zie stap 3).
2. Publiceer de publieke sleutel naar een keyserver die Central controleert:
   ```
   gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
   ```
3. Exporteer de private key ge-armored voor de GitHub-secret:
   ```
   gpg --export-secret-keys --armor <KEY_ID> > verbonden-release-key.asc
   ```
   Bewaar de inhoud van dit bestand (en de passphrase) in de
   teamwachtwoordmanager — GitHub-secrets zijn write-only en niet meer uit te
   lezen, dus zonder eigen backup ben je de sleutel kwijt bij verlies van het
   secret of bij rotatie over 2 jaar. Verwijder het lokale bestand daarna.
4. Genereer een Central Portal user token op
   [central.sonatype.com](https://central.sonatype.com/) via
   Account → Generate User Token.
5. Controleer/vraag lidmaatschap van de `nl.rotterdam`-namespace voor die
   account.
6. Voeg de 4 secrets toe onder **Settings → Secrets and variables → Actions**
   op deze repository (niet organisatiebreed):
   - `GPG_PRIVATE_KEY`
   - `GPG_PASSPHRASE`
   - `MAVEN_CENTRAL_USERNAME`
   - `MAVEN_CENTRAL_PASSWORD`

## Een release maken

1. Ga naar **Actions → Release to Maven Central → Run workflow**.
2. Vul de te releasen versie in (bijv. `1.0.0`) en start de workflow.
3. De workflow zal:
   - De opgegeven versie zetten in alle modules.
   - Bouwen en de volledige testsuite draaien (`mvnw verify`).
   - `verbonden`, `verbonden-core` en `verbonden-remote-local` signeren, van
     sources-/javadoc-jars voorzien en als één deployment naar Maven Central
     deployen (`mvnw -Prelease deploy`).
   - De versiebump committen en een tag `v<versie>` naar `main` pushen.

Er is geen automatische bump terug naar een `-SNAPSHOT`-versie. Wil je
verdergaan met ontwikkelen op een nieuwe snapshot-versie, dispatch de
workflow dan opnieuw met bijvoorbeeld `1.0.1-SNAPSHOT`.

## Een release verifiëren

- Controleer de deploymentstatus op
  [central.sonatype.com](https://central.sonatype.com/publishing/deployments) —
  moet op `PUBLISHED` staan.
- Publieke beschikbaarheid op
  [search.maven.org](https://search.maven.org/search?q=g:nl.rotterdam.verbonden)
  kan tot ~30 minuten duren na publicatie.

## Troubleshooting

- **Validatiefout door ontbrekende metadata**: Central vereist `name`,
  `description`, `url`, `licenses`, `developers` en `scm` op elke
  gepubliceerde POM. Deze staan in de `pom.xml` van elke module.
- **Ontbrekende sources-/javadoc-jar**: `maven-source-plugin` en
  `maven-javadoc-plugin` zijn geactiveerd in het `release`-profiel van
  `core/pom.xml` en `remote-local/pom.xml`.
- **Signeerfout**: controleer of de secrets `GPG_PRIVATE_KEY` en
  `GPG_PASSPHRASE` nog kloppen en of de sleutel niet verlopen is.
- **Push van de release-commit/tag mislukt na publicatie**: de workflow
  probeert de push 3x met een fetch + rebase ertussen (bijv. bij een
  gelijktijdige merge naar `main`). Faalt dat alsnog, dan is het artefact
  al gepubliceerd op Central maar staan de versiebump-commit en de tag
  `v<versie>` alleen nog lokaal op de runner. Herstel handmatig: haal de
  workflow-logs erbij voor de exacte commit-inhoud (of herhaal lokaal de
  versiebump met `versions-maven-plugin:set -DnewVersion=<versie>
  -DprocessAllModules`), commit, tag met `v<versie>`, en push beide naar
  `main`.

## Credentials roteren

Roteer de GPG-sleutel en/of het Sonatype-token in elk van deze gevallen:

- Een teamlid dat toegang had tot de secrets verlaat het team.
- Vermoeden van compromittering (bijv. een secret per ongeluk gelekt in logs,
  een fork, of een issue/PR).
- Een GPG-sleutel nadert zijn verloopdatum.
- Periodiek volgens het beveiligingsbeleid van het team (indien van
  toepassing).

Rotatie kan zonder downtime: bestaande gepubliceerde artefacten blijven
geldig ondertekend met de oude sleutel; alleen *nieuwe* releases gebruiken de
nieuwe sleutel/het nieuwe token.

### GPG-sleutel roteren

1. Genereer een nieuw sleutelpaar (zie stap 1 hierboven).
2. Publiceer de nieuwe publieke sleutel naar een keyserver (zie stap 2).
3. Exporteer de nieuwe private key en overschrijf de secrets
   `GPG_PRIVATE_KEY` en `GPG_PASSPHRASE` in de repository-instellingen.
4. Doe een test-release om te bevestigen dat signeren met de nieuwe sleutel
   werkt.
5. Bij compromittering: roep de oude sleutel in met een revocation
   certificate en publiceer die revocatie naar dezelfde keyserver(s):
   ```
   gpg --output revoke.asc --gen-revoke <OUD_KEY_ID>
   gpg --keyserver keyserver.ubuntu.com --send-keys <OUD_KEY_ID>
   ```
   (Zonder compromittering hoeft de oude sleutel niet ingetrokken te worden —
   laten verlopen volgens de gekozen expiry is voldoende.)
6. Verwijder de oude private key lokaal/uit wachtwoordmanagers zodra de
   rotatie bevestigd is.

### Sonatype/Central Portal-token roteren

1. Log in op [central.sonatype.com](https://central.sonatype.com/) met de
   account die dit repository beheert.
2. Genereer een nieuw user token (Account → Generate User Token). Dit
   vervangt eventuele bestaande tokens voor die account.
3. Overschrijf de secrets `MAVEN_CENTRAL_USERNAME` en
   `MAVEN_CENTRAL_PASSWORD` in de repository-instellingen met het nieuwe
   token.
4. Doe een test-release om te bevestigen dat authenticatie werkt.
5. Het oude token is na het genereren van een nieuw token al ongeldig
   (Sonatype staat doorgaans één actief token per account toe) — er is geen
   aparte intrekkingsstap nodig, maar controleer dit in de accountinstellingen.

### Na elke rotatie

- Werk dit document niet bij met de daadwerkelijke sleutel-ID's/tokens (die
  horen nergens in git), maar noteer eventueel wel de rotatiedatum en reden
  in de wachtwoordmanager/het logboek van het team, niet in deze repository.
- Zorg dat de rotatie **niet gelijktijdig** gebeurt met een release-dispatch,
  om te voorkomen dat een release halverwege met verlopen credentials
  faalt.
