# Checklist organisatorische stappen open source publicatie

Dit document is bedoeld voor de projectleiding (Rutger) en behandelt de
organisatorische en bestuurlijke stappen die nodig zijn vóórdat deze
repository open source gepubliceerd wordt. Dit zijn beslissingen die bij de
projectleiding en de OSPO (Open Source Program Office) van Gemeente
Rotterdam liggen — geen technische of documentatietaken.

De technische/documentatieve voorbereiding (LICENSE, `publiccode.yml`,
`SECURITY.md`, `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, README) is al op
orde. Zie [publiccode.yml](../publiccode.yml) voor de projectmetadata.

Afgeleid van de
[Project Launch Checklist](https://www.linuxfoundation.org/resources/open-source-guides/starting-an-open-source-project)
van de Linux Foundation, zoals aangereikt door de `don-open-source` skill
(developer.overheid.nl).

## 1. Overwegingen vooraf

- [ ] Is er al een bestaand open source project waar dit project beter bij
      kan aansluiten, in plaats van een nieuwe eigen repository te
      publiceren?
- [ ] Hoe realistisch is het dat andere partijen (andere gemeenten,
      leveranciers) actief mee gaan ontwikkelen?

## 2. Business strategie & plan

- [ ] Wat is het doel van het open sourcen van dit project (transparantie,
      hergebruik door andere gemeenten, wettelijke/beleidsverplichting)?
- [ ] Wie is de beoogde doelgroep (andere gemeenten, burgers, developers)?
- [ ] Wat is de toegevoegde waarde voor Gemeente Rotterdam om dit specifiek
      te open sourcen?
- [ ] Is er akkoord van de **OSPO** en de **enterprise-architecten** van
      Rotterdam op dit plan?

## 3. Onderhoudscommitment

- [ ] Wie draagt de kosten voor beheer, infrastructuur en eventuele
      community-ondersteuning na publicatie?
- [ ] Is er een commitment van **minimaal 3 jaar** om het project te blijven
      onderhouden? (Dit is een expliciete eis uit de Linux Foundation
      checklist.)

## 4. Juridische beoordeling

- [ ] Heeft open sourcen impact op het intellectueel eigendom van de
      gemeente of van derden (bijv. gebruikte gemeentelijke
      NLDS-huisstijl-assets, eventuele leverancierscode of -contracten)?
- [ ] Is de gekozen licentie (EUPL-1.2) formeel akkoord bevonden door de
      juridische afdeling?
- [ ] Zijn er handelsmerk-gerelateerde aandachtspunten (bijv. gebruik van
      "Gemeente Rotterdam"-naam/logo, of de `MockDigiD`-functionaliteit die
      een mock van de DigiD-koppeling is)?

## 5. Governance en processen

- [ ] Is er een aangewezen community advocate / contactpersoon voor externe
      bijdragers?
- [ ] Is er een publieke roadmap voor het project?
- [ ] Zijn er communicatiekanalen ingericht voor de community (naast GitHub
      issues), zoals een mailinglist?

## 6. Launch

- [ ] Zijn alle bovenstaande punten afgestemd en akkoord bevonden voordat de
      repository publiek wordt gezet?
