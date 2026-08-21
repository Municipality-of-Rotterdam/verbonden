# Voorstel: opsplitsen in Maven-modules

Dit document beschrijft een voorstel om de huidige single-module Maven-build
op te splitsen in meerdere modules. Het is een voorstel ter bespreking, geen
uitgevoerde wijziging.

## Aanleiding

Er zijn twee problemen met de huidige structuur:

1. **Testdata en testpagina's staan in productiecode.** `MockDigiDLoginPage`
   (met een hardcoded lijstje nep-BSN's) en de `MOCK_PERSONEN`-map in
   `MarriageIntakeServiceImpl` staan gewoon in `src/main/java` van de enige
   module. Dat hoort niet in productiecode.
2. **Er is geen publieke, herbruikbare contractlaag (SPI) waarmee Gemeente
   Rotterdam — in een eigen, besloten repository — een echte koppeling met
   DigiD en Haal Centraal (of interne Rotterdamse persoonsregistratie-API's)
   kan bouwen, zonder de open source broncode te hoeven forken.** Vandaag
   bestaat hiervoor geen enkele schakelmogelijkheid: een grep op `@Profile`,
   `@ConditionalOnProperty` en `CommandLineRunner` in `src/main/java` levert
   nul resultaten op. De mock-DigiD-inlogpagina wordt onvoorwaardelijk
   gemount (`WicketApplication.init()`, `mountPage("/inloggen",
   MockDigiDLoginPage.class)`), en `SecurityConfig.burgerSecurityFilterChain()`
   is één hardcoded `@Bean` — de Javadoc van die methode beschrijft nu al de
   bedoelde productie-swap (`.oauth2Login(...)` in plaats van de
   mock-entrypoint), maar er is geen mechanisme om die swap daadwerkelijk te
   maken zonder de code handmatig te wijzigen.

Dit voorstel hoort bij branch `feat/modules-for-remotes`.

## Belangrijkste koerswijziging t.o.v. eerdere versie

- **`remote-production` wordt geen onderdeel van deze (open source)
  repository.** Die module — de echte koppeling met DigiD-OIDC en met
  Rotterdamse persoonsgegevens-API's (Haal Centraal of een intern
  Rotterdams alternatief) — komt in een **andere, besloten repository**
  binnen Gemeente Rotterdam. Die repository blijft privé; deze repository
  (`huwelijk-poc-copilot`) bevat er geen enkele code, stub of referentie
  naar.
- **De modules die wél in deze repository blijven (`core`,
  `remote-local`) worden open source gereleased én gepubliceerd op Maven
  Central**, zodat de besloten repository ze als gewone Maven-dependency
  kan consumeren in plaats van de code te kopiëren of te forken.
- **De `app-local`-module in deze repository draait altijd op `remote-local`.**
  Er zijn geen Maven-profielen nodig — `app-local` heeft een gewone,
  onvoorwaardelijke dependency op `remote-local`. Er is binnen deze
  repository niets om tussen te schakelen, want de productie-variant
  bestaat hier niet.

Dit maakt van deze repository, in effect, een **open source referentie-
implementatie inclusief publieke SPI**, en van de besloten repository een
**dunne, Rotterdam-specifieke adapter** die tegen die SPI aan bouwt.

## Voorstel: drie Maven-modules

De root `pom.xml` wordt een reactor-`pom` (`packaging=pom`), blijft
`spring-boot-starter-parent` als parent gebruiken en behoudt de
`dependencyManagement`. Daaronder komen drie submodules:

```
huwelijk-poc-copilot/                     (parent pom, packaging=pom)
├── pom.xml
├── core/                                 (nl.rotterdam.huwelijk:huwelijk-core, packaging=jar)
│   │                                      → gepubliceerd op Maven Central
│   └── src/main/java/nl/rotterdam/huwelijk/
│       ├── WicketApplication.java         (mount-lijst i.p.v. hardcoded mock)
│       ├── config/SecurityConfig.java     (burgerSecurityFilterChain blijft hier,
│       │                                    nu samenstelbaar i.p.v. hardcoded mock)
│       ├── domain/, persistence/
│       ├── administration_common/, burger_common/
│       ├── identity/                      ← nieuw: de "poorten" (SPI), publiek contract
│       │   ├── AuthenticatedUser.java      (userId + roles, ongeacht burger/medewerker)
│       │   ├── CurrentUserProvider.java    (+ standaardimplementatie, leest SecurityContext)
│       │   ├── Roles.java                  (BURGER, SUPERUSER, BABS, …)
│       │   ├── PersonLookupService.java
│       │   ├── PersonInfo.java
│       │   └── BurgerLoginPageMount.java
│       └── features/                      (alle features, zonder mock_digid)
├── remote-local/                         (nl.rotterdam.huwelijk:huwelijk-remote-local, dep: core)
│   │                                      → gepubliceerd op Maven Central
│   └── src/main/java/nl/rotterdam/huwelijk/
│       ├── features/mock_digid/ui/        (MockDigiDLoginPage + .html, ongewijzigd)
│       └── remote_local/
│           ├── MockPersonLookupService.java (bevat de verplaatste MOCK_PERSONEN)
│           └── MockDigiDLoginPageMount.java (aan/uit via digid.mock-login.enabled)
└── app-local/                            (nl.rotterdam.huwelijk:huwelijk-app-local, packaging=jar, uitvoerbaar)
    │                                      → onvoorwaardelijke dependency op core + remote-local
    └── src/main/java/nl/rotterdam/huwelijk/HuwelijkApplication.java
```

`remote-production` staat niet in deze boom: die module wordt gebouwd in de
besloten repository, tegen de hier gepubliceerde `huwelijk-core` als
Maven Central-dependency. Zie "De besloten `remote-production`-repository"
hieronder.

### `core` — gedeelde productiecode, publiek contract

Bevat alles wat ongewijzigd blijft: alle `features.*`-packages (behalve
`mock_digid`), `persistence`, `domain`, `administration_common`,
`burger_common`, `config`, `WicketApplication`, en de Flyway-migraties. Deze
module is op zichzelf niet start-baar — dat is bewust, het dwingt af dat er
altijd een adapter-module (`remote-local` hier, of `remote-production` in de
besloten repo) wordt meegebouwd.

`core` introduceert de "poorten" waar de rest van de codebase tegen
programmeert, in een nieuwe package `identity`:

- **`AuthenticatedUser`** (interface) — `getUserId()` + `getRoles(): Set<String>`
  (plus een default `hasRole(String)`). Eén representatie voor zowel een
  ingelogde burger (DigiD) als een ingelogde medewerker (beheer), ongeacht
  hoe de authenticatie tot stand kwam. Genoemd `AuthenticatedUser` en niet
  `AuthenticatedPrincipal`, om verwarring met Spring Security's eigen
  `org.springframework.security.core.AuthenticatedPrincipal` te vermijden,
  en omdat "user" voor zowel burger als medewerker natuurlijker leest dan
  het meer technische "principal".
- **`CurrentUserProvider`** (interface, met één gedeelde implementatie in
  `core` die `SecurityContextHolder` leest) — vertaalt de actieve
  `Authentication` (van welke bron dan ook: mock-DigiD, echte
  DigiD-OIDC, of het admin form-login) naar een `AuthenticatedUser`, door
  `ROLE_`-`GrantedAuthority`'s om te zetten naar de rollen-set. Dit wordt
  hét aanspreekpunt voor applicatiecode, in plaats van de losse
  `BurgerBasePage.getCurrentBsn()` en de directe
  `SecurityContextHolder`-aanroepen in bijvoorbeeld
  `LocationAdministrationServiceImpl`.
- **`Roles`** — constanten voor de rollen die `getRoles()` kan teruggeven:
  `BURGER` ("Burger") voor een ingelogde burger, en voorbeelden voor
  medewerkers zoals `SUPERUSER` ("superuser") en `BABS` ("babs"). Vandaag
  kent `SecurityConfig.userDetailsService()` nog maar één rol
  (`BEHEERDER`) toe aan alle beheerders; het uitbreiden naar rollen als
  `superuser`/`babs` per medewerker is autorisatiewerk dat buiten deze PR
  valt, maar de vorm van `AuthenticatedUser.getRoles()` is er vanaf het
  begin op ingericht.
- **`PersonLookupService`** (interface) + **`PersonInfo`** (record) —
  vervangt de inline `MOCK_PERSONEN`-map. `MarriageIntakeServiceImpl` krijgt
  deze via de constructor geïnjecteerd in plaats van de hardcoded map te
  gebruiken.
- **`BurgerLoginPageMount`** (interface: pad + pagina-klasse) —
  `WicketApplication` krijgt een `List<BurgerLoginPageMount>` in de
  constructor en mount elke entry in `init()`, in plaats van de hardcoded
  `mountPage("/inloggen", MockDigiDLoginPage.class)`-regel.
- **`SecurityConfig`** blijft in `core`, inclusief `burgerSecurityFilterChain`
  — zie "Mock-login blijft werken naast OIDC" hieronder voor waarom die
  niet meer per adapter-module wordt gedupliceerd.

**Let op:** de package `identity` valt buiten de package-conventie uit
`AGENTS.md` (`features.<naam>.{ui,application,domain,repository}`), omdat
het geen feature is maar een cross-cutting contract dat door meerdere
features en door `WicketApplication`/`config` gebruikt wordt. Dit is een
bewuste, gedocumenteerde uitzondering. Voorstel: `AGENTS.md` uitbreiden met
een korte paragraaf die "adapter SPI"-packages als erkende categorie
beschrijft, zodat toekomstige uitzonderingen hierop kunnen aansluiten.

Omdat de besloten `remote-production`-repository straks tegen de
gepubliceerde `huwelijk-core`-versie bouwt, is `identity` niet langer alleen
een interne modulegrens maar een **echt publiek API-contract tussen twee
repositories**. Zie "Versiebeleid identity-SPI" hieronder.

### `remote-local` — lokale ontwikkeling én publieke referentie-implementatie

Hangt af van `core`. Hier landen alle testdata en testpagina's, buiten
`core`:

- `MockDigiDLoginPage` (+ `.html`), ongewijzigd verplaatst — de
  authenticatie-logica (token bouwen, rol `Burger` toekennen) blijft
  hetzelfde als vandaag.
- `MockPersonLookupService`, implementeert `PersonLookupService`, bevat de
  verplaatste `MOCK_PERSONEN`-map.
- `MockDigiDLoginPageMount`, implementeert `BurgerLoginPageMount` voor
  `/inloggen` → `MockDigiDLoginPage`, actief zolang de property
  `digid.mock-login.enabled` niet expliciet op `false` staat (default
  `true`).

`remote-local` wordt, net als `core`, op Maven Central gepubliceerd. Dat
dient twee doelen: het is de dependency waar `app-local` in déze repository altijd
op draait, én het dient als publiek voorbeeld van hoe een `identity`-SPI-
implementatie eruitziet — nuttig voor andere gemeenten die dezelfde
huwelijksapp zouden willen draaien, en als referentie voor wie
`remote-production` bouwt.

De mock-login is niet hard-exclusief met een echte OIDC-login (zie "Mock-
login blijft werken naast OIDC" hieronder) — dat blijft ook relevant nu
`remote-production` in een andere repository zit: die repository kan zijn
eigen uitvoerbare module laten kiezen of hij, naast een echte
OIDC-koppeling, ook `remote-local` meebouwt (bijvoorbeeld voor een
acceptatieomgeving).

### `app-local` — het uitvoerbare artefact in deze repository

Bevat `HuwelijkApplication` (de `@SpringBootApplication`-hoofdklasse),
`application*.properties`, en de `spring-boot-maven-plugin` (repackage
alleen hier gebonden). Heeft een gewone, onvoorwaardelijke `<dependency>`
op `core` én `remote-local` — **geen Maven-profielen**, want er is binnen
deze repository niets anders om naar te schakelen. `./mvnw spring-boot:run
-pl app-local -am` werkt zonder extra vlaggen.

Hier landen ook de testen die een volledige Spring-context of
`WicketTester` nodig hebben (9 van de 14 huidige testbestanden):
`HuwelijkApplicationTests`, `BaseWicketTest`, de `HuwelijkIntegrationTest`-
annotatie, `BabsImportServiceImplTestIntegration`,
`BeschikbareSlotsIntegrationTest`, en de vijf Wicket-UI-tests
(`ContactGegevensFormTest`, `DatumKiezenPageTest`, `DeGetuigenPageTest`,
`JullieGegevensPageTest`, `MarriageIntakePageTest`). De overige 4 (zuivere
unit tests zonder Spring-context: `EmailadresTest`, `TelefoonnummerTest`,
`GetuigenItemFormDtoTest`, `NietBeschikbareDagImportServiceImplTest`)
blijven in `core`, naast de productiecode die ze testen.

`app-local` zelf wordt **niet** als bruikbare library-dependency op Maven Central
gepubliceerd (het is een uitvoerbaar Spring Boot-artefact, niemand
consumeert het als dependency) — zie "Maven Central publicatie" hieronder
voor de afweging om 'm eventueel toch te publiceren, als lage-prioriteit
vervolgstap.

## De besloten `remote-production`-repository

Dit is geen onderdeel van deze repository of dit voorstel, maar wordt hier
beschreven om de grens tussen beide expliciet te maken:

- Nieuwe, besloten repository binnen Gemeente Rotterdam (naam/locatie nog te
  bepalen). Niet open source, geen onderdeel van `huwelijk-poc-copilot`.
- Bevat een module die de `identity`-SPI uit `core` implementeert tegen
  echte interne Rotterdamse API's: DigiD-OIDC voor authenticatie, en
  Haal Centraal of een Rotterdam-eigen persoonsregistratie-API voor
  `PersonLookupService` (zie [beoogde-koppelingen.md](beoogde-koppelingen.md)
  voor de status van die koppelingen).
- Consumeert `nl.rotterdam.huwelijk:huwelijk-core` (en optioneel
  `huwelijk-remote-local`, voor een acceptatieomgeving) als gewone Maven
  Central-dependency — geen source-copy, geen fork.
- Heeft daar zijn eigen uitvoerbare module nodig (analoog aan `app-local` in deze
  repository, maar met de eigen `remote-production`-module i.p.v.
  `remote-local` als dependency, en een eigen `HuwelijkApplication`-achtige
  hoofdklasse). De constructie daarvan is aan die repository — buiten scope
  hier.
- Bouwt uitsluitend tegen het **publieke** Maven Central, geen interne
  Nexus-mirror (bewuste keuze, zie "Risico's" hieronder voor de
  beschikbaarheids-afweging die dat met zich meebrengt).

## Mock-login blijft werken naast OIDC

Een eerdere versie van dit voorstel behandelde de DigiD-login als strikt
exclusief: óf de mock-inlogpagina, óf een echte OIDC-provider, geswapt via
welke Maven-module wordt meegebouwd. Dat is losgelaten voor de login zelf —
het is prima als de mock-DigiD-login blijft functioneren, ook wanneer een
echte OIDC-provider is geconfigureerd, bijvoorbeeld handig voor
acceptatie-/testomgevingen die verder wél tegen echte infrastructuur
draaien.

Concreet:

- `burgerSecurityFilterChain` (in `core`, `SecurityConfig`) is er nog maar
  één. De chain staat `/inloggen/**` altijd toe, en voegt
  `.oauth2Login(...)` toe zodra er een `ClientRegistrationRepository`-bean
  aanwezig is (die de besloten repo's `remote-production`-module
  meebrengt).
- Het mounten van `MockDigiDLoginPage` (via `BurgerLoginPageMount`) wordt
  aan- of uitgezet met de property `digid.mock-login.enabled`, niet met
  welke module er is meegebouwd. Wie `remote-local` samen met een echte
  OIDC-koppeling meebouwt (zoals de besloten repo zou kunnen doen voor een
  acceptatieomgeving), krijgt dus desgewenst beide inlogroutes tegelijk.
- Beide inlogroutes zijn verantwoordelijk voor het toekennen van dezelfde
  rol (`Roles.BURGER`) aan de resulterende `Authentication`, zodat
  `CurrentUserProvider`/`AuthenticatedUser` zich identiek gedraagt ongeacht
  welke inlogroute gebruikt is.

`PersonLookupService` (Haal Centraal/Rotterdamse API versus
mock-persoonsgegevens) blijft wél strikt module-exclusief zoals hieronder
beschreven — daar leidt twee actieve implementaties tot een dubbelzinnige
autowiring, en dat risico is gewenst om vroeg en luid te falen in plaats van
stil verkeerd te gaan.

## Waarom classpath-gestuurd swappen, en geen `@Profile`

Voor `PersonLookupService` geldt: omdat er per build van een uitvoerbare
module (`app-local` hier, of de besloten repo's productie-equivalent) altijd
precies één adapter-module als dependency aanwezig is, vindt Spring's
classpath component scan vanzelf precies één bean voor die interface. Dat
is eenvoudiger dan `@Profile`-vlaggen combineren met modulegrenzen. Binnen
déze repository is dit nu een niet-vraagstuk (`app-local` heeft altijd precies
`remote-local`), maar het patroon is wel de reden dat de besloten repo geen
`@Profile`-gedoe nodig heeft om zijn eigen `remote-production` te laten
werken. (Voor de DigiD-login geldt dit niet — die schakelt op een property,
zie hierboven.)

Dit vertrouwt wel op één aanname: `HuwelijkApplication` (en het equivalent
in de besloten repo) heeft geen expliciete `scanBasePackages`, dus het
default scan-basispakket is `nl.rotterdam.huwelijk`. Nieuwe adapter-modules
— ook `remote-production` in de besloten repo — moeten hun code onder dat
package-root houden — of, als extra zekerheid, kan `@SpringBootApplication`
expliciet `scanBasePackages = "nl.rotterdam.huwelijk"` meekrijgen.

Een verkeerde configuratie van `PersonLookupService` (bijvoorbeeld per
ongeluk beide adapter-modules tegelijk op het classpath) faalt luid bij het
opstarten van de Spring-context — een dubbelzinnige bean bij het
injecteren in `MarriageIntakeServiceImpl`. Dat is een acceptabel vangnet,
aangezien het samenstellen van dependencies het eigenlijke, bedoelde
schakelmechanisme is.

## Maven Central publicatie

- **Namespace:** `nl.rotterdam.huwelijk`, te verifiëren via een DNS
  TXT-record op (een subdomein van) `rotterdam.nl`. Dit vereist coördinatie
  met wie DNS voor `rotterdam.nl` beheert, en moet geregeld zijn vóór de
  eerste release. **Open actiepunt**, geen technische blocker voor de
  module-opsplitsing zelf.
- **Gepubliceerde artifacts:** `huwelijk-core` en `huwelijk-remote-local` —
  dit zijn de modules die de besloten repository daadwerkelijk als
  dependency nodig heeft (`core`), resp. als referentie-implementatie kan
  gebruiken (`remote-local`). `huwelijk-app-local` wordt niet meegenomen in de
  eerste release; publiceren kan later alsnog als lage-prioriteit
  vervolgstap, maar heeft weinig nut zolang niemand het als dependency
  gebruikt.
- **Mechaniek:** publicatie via de Sonatype Central Portal (de oudere
  OSSRH/`s01` route is uitgefaseerd), met de
  `central-publishing-maven-plugin`. Vereist per gepubliceerde module in de
  POM: `<name>`, `<description>`, `<url>`, `<licenses>`, `<developers>`,
  `<scm>`, plus GPG-signing en verplichte sources- en javadoc-jars.
- **Open vraag — sleutel- en accountbeheer:** wie beheert het
  Sonatype-account (namespace-ownership) en de GPG-signing-sleutel?
  Voorstel: een door Gemeente Rotterdam beheerd account en sleutel, releases
  getriggerd via een GitHub Actions-workflow op een git tag (bv.
  `v0.0.5-alpha`), sleutel als repository secret. Dit moet organisatorisch
  nog belegd worden.
- **Versiebeleid:** alpha-versienummers, zie hieronder.

## Versiebeleid identity-SPI (alpha, tot 1.0)

We zijn voorlopig nog in een alpha-/bètafase. Dat betekent: gewone,
oplopende Maven-versienummers met een `-alpha`-suffix — `0.0.5-alpha`,
`0.0.6-alpha`, enzovoort — en **geen** backward-compatibiliteitsgarantie op
`core.identity` (of enige andere publieke klasse in `core`) zolang de
major-versie op `0` staat.

Praktisch gevolg:
- Een breaking change in `identity` (methode-signatuur wijzigen, een
  interface-methode verwijderen, een gedragswijziging die een bestaande
  implementatie stuk maakt) is toegestaan in elke `0.0.x-alpha`-release,
  zonder major-bump — er is nog geen contract om te breken.
- De besloten `remote-production`-repository pint een specifieke
  `0.0.x-alpha`-versie van `huwelijk-core` en werkt die bewust bij, in
  plaats van erop te vertrouwen dat elke nieuwe alpha-release
  backward-compatible is.
- Geen CHANGELOG-verplichting per wijziging in `identity` zolang we in de
  alpha-/bètafase zitten — dat komt terug zodra we naar `1.0` gaan.

**Vanaf `1.0.0` gaat gewone semver gelden op `identity`** (patch =
bugfix zonder API-wijziging, minor = additief, major = breaking change) —
inclusief een CHANGELOG-verplichting per wijziging. Tot die tijd is elke
`0.x`-release een momentopname; wie ertegen bouwt (ook de besloten repo)
accepteert dat.

## Build-mechanica: aandachtspunten

Dit zijn de punten die de build stilletjes kunnen breken als ze over het
hoofd worden gezien:

- **`spring-boot-maven-plugin` moet naar `<pluginManagement>`.** De huidige
  root-`pom.xml` bindt de plugin direct in `<build><plugins>`. Als dat zo
  blijft nadat de root een reactor-`pom` wordt, probeert Maven de
  `repackage`-goal op elke module uit te voeren — ook op `core` en
  `remote-local`, die geen hoofdklasse hebben. Dat breekt `mvn verify`
  direct. Oplossing: de plugin-declaratie naar de `<pluginManagement>` van
  de parent-pom, en alleen in `app-local`'s eigen `pom.xml` een `<plugin>`-entry
  (zonder versie, die komt uit pluginManagement).
- **`./mvnw spring-boot:run` vanaf de repo-root breekt.** Een
  reactor-`pom` heeft zelf geen `spring-boot:run`-goal. Dit wordt
  `./mvnw spring-boot:run -pl app-local -am` — zonder profiel-vlaggen, want `app-local`
  heeft maar één configuratie. `README.md` moet hierop worden aangepast
  (sectie "Start de Spring Boot applicatie"), inclusief het pad naar
  `application.properties` in de `beheer.gebruikers`-uitleg
  (`app-local/src/main/resources/application.properties`).
- **`./mvnw verify` vanaf de repo-root blijft ongewijzigd werken.** Een
  Maven-reactor bouwt alle modules in afhankelijkheidsvolgorde. CI
  (`.github/workflows/ci.yml`) hoeft dus niet aangepast te worden voor het
  bestaande gedrag. Nieuw als vervolgstap: een release-workflow die bij een
  git tag de Central-publicatie van `core` en `remote-local` triggert (zie
  "Maven Central publicatie").
- **Resource-inheritance werkt vanzelf.** De huidige `<build><resources>`
  in `pom.xml` (die `src/main/java/**/*.html`/`.properties`/`.xml`/`.css`
  als resource meeneemt, voor de co-located Wicket-markup) wordt door
  Maven automatisch overgenomen door elke child-module, opgelost tegen de
  basedir van die child. Geen per-module boilerplate nodig.

## Concrete bestandsverplaatsingen

**Verplaatst, ongewijzigd:**
- `HuwelijkApplication.java`, `application.properties`,
  `application-local.properties` → `app-local`
- `MockDigiDLoginPage.java` + `.html` → `remote-local`
  (package blijft `nl.rotterdam.huwelijk.features.mock_digid.ui`)
- De 9 Spring-context/Wicket-tests hierboven → `app-local`
- Al het overige (persistence, domain, features minus mock_digid,
  administration_common, burger_common, config, WicketApplication +
  converters, Flyway-migraties, de 4 unit tests) → `core`

**Gewijzigd:**
- `SecurityConfig.java` (blijft in `core`): `burgerSecurityFilterChain()`
  blijft bestaan maar wordt samenstelbaar — staat `/inloggen/**` altijd
  toe en voegt `.oauth2Login(...)` toe zodra een
  `ClientRegistrationRepository`-bean aanwezig is.
- `WicketApplication.java` (blijft in `core`): constructor met
  `List<BurgerLoginPageMount>`, mount-loop i.p.v. hardcoded
  `MockDigiDLoginPage`-mount.
- `BurgerBasePage.java` (blijft in `core`): `getCurrentBsn()` vervangen
  door gebruik van `CurrentUserProvider`/`AuthenticatedUser.getUserId()`.
- `LocationAdministrationServiceImpl.java`,
  `NietBeschikbareDagImportServiceImpl.java` (blijven in `core`): directe
  `SecurityContextHolder`-aanroepen vervangen door
  `CurrentUserProvider`/`AuthenticatedUser.getUserId()`, voor consistentie.
- `MarriageIntakeServiceImpl.java` (blijft in `core`): `MockPersonInfo` +
  `MOCK_PERSONEN` + `retrievePersonInfo` verwijderd, vervangen door
  geïnjecteerde `PersonLookupService`.
- `README.md`: `spring-boot:run`-instructies zoals hierboven, plus een korte
  toelichting dat `core`/`remote-local` op Maven Central gepubliceerd worden
  en dat een productie-adapter in een aparte, besloten repository leeft.

**Nieuw:**
- `core`: `identity/AuthenticatedUser.java`,
  `identity/CurrentUserProvider.java` (+ standaardimplementatie),
  `identity/Roles.java`, `identity/PersonLookupService.java`,
  `identity/PersonInfo.java`, `identity/BurgerLoginPageMount.java`
- `remote-local`: `remote_local/MockPersonLookupService.java`,
  `remote_local/MockDigiDLoginPageMount.java`
- Drie nieuwe `pom.xml`-bestanden, één per module, met voor `core` en
  `remote-local` de aanvullende POM-metadata die Maven Central vereist
  (`<name>`, `<description>`, `<url>`, `<licenses>`, `<developers>`,
  `<scm>`).

## Expliciet buiten scope

- `application-local.properties` (H2-override): blijkt momenteel dood/niet
  aangesloten (nergens een `spring.profiles.active` of `@ActiveProfiles` in
  de repo). Los hiervan, wordt niet meegenomen in dit voorstel.
- **De `remote-production`-module zelf, volledig.** Die wordt gebouwd in de
  besloten repository, niet hier — inclusief de echte DigiD-OIDC- en
  persoonsgegevens-koppelingen. Zie "De besloten `remote-production`-
  repository" hierboven.
- Het daadwerkelijk toekennen van fijnmazige medewerkerrollen (`superuser`,
  `babs`, …). `SecurityConfig.userDetailsService()` kent vandaag iedere
  beheerder dezelfde rol (`BEHEERDER`) toe; `Roles` in dit voorstel
  beschrijft de bedoelde vorm, maar het uitbreiden van
  `userDetailsService` (of de toekomstige medewerkers-OIDC-koppeling, zie
  beoogde-koppelingen.md) naar rol-per-medewerker is losstaand
  autorisatiewerk.
- De admin/`beheer`-inlogflow (form login, `InMemoryUserDetailsManager`)
  blijft ongewijzigd qua mechanisme — alleen het uitlezen ervan verhuist
  naar `CurrentUserProvider`, zoals hierboven beschreven.
- Het daadwerkelijk regelen van DNS-verificatie, Sonatype-account en
  GPG-sleutelbeheer voor Maven Central — organisatorische vervolgstappen,
  zie "Maven Central publicatie".

## Risico's en alternatieven

- **Modulegrenzen en Spring-profielen sluiten elkaar niet uit.** De
  besloten `remote-production`-module zou later intern
  `@ConditionalOnProperty` kunnen gebruiken om bijvoorbeeld tussen een
  echte en een sandbox-Haal-Centraal-client te kiezen, zonder daar een
  aparte module voor nodig te hebben.
- **`digid.mock-login.enabled` per ongeluk aan laten staan in een echte
  productieomgeving.** Dit blijft relevant voor de besloten repo, mocht die
  ervoor kiezen `remote-local` mee te bouwen naast `remote-production`.
  Doordat de mock-login een property is in plaats van een module-swap, moet
  dit expliciet op `false` staan in de productieconfiguratie; een
  ontbrekende of foutieve waarde valt niet vanzelf op. Aanbevolen: default
  `true` (zoals vandaag), met een duidelijke vermelding in het
  deploymentproces van de besloten repo dat dit expliciet op `false` moet
  worden gezet.
- **Publieke Maven Central als enige artifact-bron voor de besloten
  productie-repo.** Gekozen is om de besloten repo uitsluitend tegen het
  publieke Central te laten bouwen, zonder interne Nexus-mirror. Risico:
  een productie-CI-build van een gemeentelijke huwelijksapplicatie hangt
  daarmee af van de beschikbaarheid van het publieke Maven Central tijdens
  elke build. Mitigatie is bewust niet nu al uitgewerkt (bijvoorbeeld een
  lokale artifact-repository-proxy in de CI-pipeline van de besloten repo)
  — dat kan later, zonder dat het iets aan déze repository verandert.
- **De alpha-aanpak schuift het compat-vraagstuk door naar `1.0`.** Door
  voorlopig met `0.0.x-alpha` te werken (zie "Versiebeleid identity-SPI")
  hoeft `identity` nu nog niet backward-compatible te blijven, wat
  ontwikkelsnelheid binnen `core` niet vertraagt. Het risico verschuift
  daarmee naar het moment dat de besloten repo daadwerkelijk in productie
  draait: vanaf dan moet elke `huwelijk-core`-upgrade daar bewust gebeuren
  (versie pinnen, wijzigingen doorlezen), in plaats van dat een patch-release
  automatisch veilig is. Dat is acceptabel zolang de besloten repo nog geen
  productieverkeer heeft; vóór dat moment moet alsnog de knoop worden
  doorgehakt over wanneer we naar `1.0` gaan en de semver-belofte ingaat.
