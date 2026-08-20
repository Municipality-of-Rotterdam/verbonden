# huwelijk-poc-copilot

POC voor het digitaliseren van huwelijksaanvragen bij de Gemeente Rotterdam.

## Features

Deze codebase bestaat uit de volgende features:

- Digitale huwelijksaangifte voor burgers
- Beheer van Buitengewoon Ambtenaren van de Burgerlijke Stand (BABS)

Dit is een proof-of-concept: de functionaliteit is nog incompleet en wordt
actief doorontwikkeld. Zie [publiccode.yml](publiccode.yml) voor de
projectmetadata.

## Ontwikkelstatus

Dit project heeft de status: **development** (proof-of-concept). Zie
[publiccode.yml](publiccode.yml).

## Documentatie

- [Applicatie-architectuur](docs/applicatie-architectuur.md)
- [Beoogde koppelingen](docs/beoogde-koppelingen.md)
- [Checklist organisatorische stappen open source publicatie](docs/open-source-publicatie-checklist.md)

## Vereisten

| Tool | Minimale versie |
|------|----------------|
| Java | 25 |
| Maven | meegeleverd via `./mvnw` |
| Docker | 24+ (voor de database) |

## Lokaal opstarten

### 1. Start de database

De applicatie gebruikt PostgreSQL. Start de database met Docker Compose:

```bash
docker compose up -d
```

De database draait dan op **poort 5433** (niet de standaard 5432, om conflicten met
een eventuele lokale PostgreSQL-installatie te vermijden).

| Parameter | Waarde |
|-----------|--------|
| Host | `localhost` |
| Poort | `5433` |
| Database | `huwelijk` |
| Gebruiker | `huwelijk` |
| Wachtwoord | `huwelijk` |

Stop de database later met:

```bash
docker compose down
```

Om ook de opgeslagen data te verwijderen:

```bash
docker compose down -v
```

### 2. Start de Spring Boot applicatie

```bash
./mvnw spring-boot:run
```

De applicatie is daarna bereikbaar op <http://localhost:8080>.

### 3. Beheerderssectie

De beheerpagina's zijn bereikbaar via <http://localhost:8080/beheer>.
Je wordt doorgestuurd naar het inlogscherm.

Standaard inloggegevens voor lokaal gebruik (geconfigureerd in `application.properties`):

| Gebruikersnaam | Wachtwoord |
|----------------|-----------|
| `beheerder` | `rotterdam` |

Extra beheerders kun je toevoegen via de eigenschap `beheer.gebruikers` in
`src/main/resources/application.properties`:

```properties
# Formaat: gebruiker1:wachtwoord1,gebruiker2:wachtwoord2
# Gebruik in productie een {bcrypt}-hash als wachtwoord:
#   beheer.gebruikers=alice:{bcrypt}$2a$10$…
beheer.gebruikers=beheerder:rotterdam
```

## Bouwen en testen

```bash
./mvnw verify
```

De tests gebruiken een embedded PostgreSQL 18 database
(via [embedded-database-spring-test](https://github.com/zonkyio/embedded-database-spring-test))
en vereisen geen draaiende Docker-container.

## Bijdragen

Leuk dat je overweegt om bij te dragen aan dit project. Lees onze
[CONTRIBUTING.md](CONTRIBUTING.md) voor meer informatie over hoe je kunt
bijdragen.

### Gedragscode

Dit project hanteert een [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md). Door bij
te dragen aan dit project ga je akkoord met de voorwaarden hiervan.

## Security

Heb je een potentieel securityissue gevonden? Fijn dat je de moeite hebt
genomen om hier in te duiken. Hoe je op een veilige manier melding kan maken
vind je in [SECURITY.md](SECURITY.md).

## Licentie

[European Union Public Licence v. 1.2 (EUPL-1.2)](https://eupl.eu/1.2/en/)

Copyright © Gemeente Rotterdam.

## Contact

Vragen over dit project? Neem contact op via
[mijnloket_ontwikkelteam@Rotterdam.onmicrosoft.com](mailto:mijnloket_ontwikkelteam@Rotterdam.onmicrosoft.com).
Zie ook [publiccode.yml](publiccode.yml) voor de projectmetadata.
