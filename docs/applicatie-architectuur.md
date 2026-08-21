# Applicatie-architectuur

Dit document beschrijft de applicatie-architectuur van `huwelijksapplicatie`.

## Technische basis

- **Build-tool:** Maven-reactor (root `pom.xml`, packaging `pom`), met Spring
  Boot parent. Drie modules: `core`, `remote-local`, `app-local`.
- **Runtime:** Java 25.
- **Frameworks:** Spring Boot, Spring Data JPA, Spring Security, Apache Wicket, Flyway, PostgreSQL.
- **UI-stijl:** Rotterdam NL Design System (NLDS) componenten en thema.

## Opbouw op hoofdlijnen

De applicatielogica (in `core`) is een monoliet met duidelijke lagen:

1. **UI (Wicket pages/components)** in `feature.<naam>.ui`.
2. **Applicatielaag (services)** in `feature.<naam>.application`.
3. **Domeinlaag (DTOs/value types)** in `feature.<naam>.domain` en `nl.rotterdam.huwelijk.domain`.
4. **Persistentielaag** met JPA entities in `nl.rotterdam.huwelijk.persistence` en repositories in `feature.<naam>.repository`.
5. **Database**: PostgreSQL, schema/migraties via Flyway.

Belangrijke architectuurregels voor de software-structuur:

- JPA entities verlaten de servicelaag niet.
- Service I/O gebruikt immutable `record` DTOs.
- Feature-packages volgen vaste sub-packages: `ui`, `application`, `domain`, `repository`.

## Architectuurprincipes (Common Ground)

- **Data bij de bron:** burgergegevens worden bij de bron opgehaald (bijvoorbeeld via Haal Centraal voor adres- en oudergegevens), in plaats van langdurig te kopiëren naar de huwelijksapplicatie.
- **Administratief compleet maken van voorgenomen huwelijk/partnerschap:** de primaire verantwoordelijkheid van deze applicatie is het compleet maken van de aanvraag.
- **Zaakgericht vervolgproces:** zodra een dossier administratief compleet is, wordt het doorgezet naar het zaaksysteem, waar het vervolg plaatsvindt in bestaande processen.
- **Documenten via ZGW document-API's:** identiteitsbewijzen en andere vereiste verklaringen worden via de ZGW document-API's opgeslagen in het zaaksysteem.

## Feature-indeling

Belangrijkste functionele modules:

- `features.marriage_intake` (burgerflow huwelijksaangifte)
- `features.babs_administration` (BABS-beheer)
- `features.location_administration` (trouwlocaties en beschikbaarheid)
- `features.marriage_type_administration` (huwelijkstypen)
- `features.dossier_administration` (dossiers)
- `features.mock_digid` (mock-inlog voor development/test)

Dit was de situatie op 22 mei 2026. De verwachting is dat er in de toekomst nog features bijkomen zoals:

- haal centraal integratie
- betalingen
- zaaksysteem integratie
- afhandelen van taken (zoals verwerken van ids en verklaringen)

Daarnaast zijn er gedeelde basislagen:

- `administration_common` (beheer-basispagina's)
- `burger_common` (burger-basispagina's)
- `config` (Spring Boot/Wicket/Security configuratie)

## Diagrammen (PlantUML)

- [Context en bouwblokken](architectuur-context.puml)
- [Lagen en afhankelijkheden](architectuur-lagen.puml)

Deze diagrammen zijn in `.puml` vastgelegd zodat ze in tooling/CI te renderen zijn.
