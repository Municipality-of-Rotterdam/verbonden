# Applicatie-architectuur

Dit document beschrijft de applicatie-architectuur van `huwelijk-poc-copilot` op basis van de codebase, build-opzet, gebruikte frameworks en de Copilot-richtlijnen.

## Technische basis

- **Build-tool:** Maven (`pom.xml`), met Spring Boot parent.
- **Runtime:** Java 25.
- **Frameworks:** Spring Boot, Spring Data JPA, Spring Security, Apache Wicket, Flyway, PostgreSQL.
- **UI-stijl:** Rotterdam NL Design System (NLDS) componenten en thema.

## Opbouw op hoofdlijnen

De applicatie is een monoliet met duidelijke lagen:

1. **UI (Wicket pages/components)** in `feature.<naam>.ui`.
2. **Applicatielaag (services)** in `feature.<naam>.application`.
3. **Domeinlaag (DTOs/value types)** in `feature.<naam>.domain` en `nl.rotterdam.huwelijk.domain`.
4. **Persistentielaag** met JPA entities in `nl.rotterdam.huwelijk.persistence` en repositories in `feature.<naam>.repository`.
5. **Database**: PostgreSQL, schema/migraties via Flyway.

Belangrijke architectuurregels uit de Copilot-richtlijnen:

- JPA entities verlaten de servicelaag niet.
- Service I/O gebruikt immutable `record` DTOs.
- Feature-packages volgen vaste sub-packages: `ui`, `application`, `domain`, `repository`.

## Feature-indeling

Belangrijkste functionele modules:

- `features.marriage_intake` (burgerflow huwelijksaangifte)
- `features.babs_administration` (BABS-beheer)
- `features.location_administration` (trouwlocaties en beschikbaarheid)
- `features.marriage_type_administration` (huwelijkstypen)
- `features.dossier_administration` (dossiers)
- `features.mock_digid` (mock-inlog voor development/test)

Daarnaast zijn er gedeelde basislagen:

- `administration_common` (beheer-basispagina's)
- `burger_common` (burger-basispagina's)
- `config` (Spring Boot/Wicket/Security configuratie)

## Diagrammen (PlantUML)

- [Context en bouwblokken](architectuur-context.puml)
- [Lagen en afhankelijkheden](architectuur-lagen.puml)

Deze diagrammen zijn in `.puml` vastgelegd zodat ze in tooling/CI te renderen zijn.
