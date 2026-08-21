# Copilot Instructions

## Architectuurregels

### Packagestructuur
De code is opgedeeld in de volgende packages:
- `nl.rotterdam.huwelijk.persistence` — bevat uitsluitend JPA Entity klassen (geen repositories).
- `nl.rotterdam.huwelijk.domain` — gedeelde domeininterfaces zoals `ValueHolder<T>`, die door meerdere feature-packages worden hergebruikt.
- `nl.rotterdam.huwelijk.features.babs_administration` — alle code voor het beheer van Buitengewoon Ambtenaren van de Burgerlijke Stand (BAPS), opgedeeld in sub-packages (zie hieronder).
- `nl.rotterdam.huwelijk.features.marriage_intake` — alle code voor het huwelijksaangifteproces door burgers, opgedeeld in sub-packages (zie hieronder).
- `nl.rotterdam.huwelijk.administration_common` — gedeelde basisklassen voor beheerpagina's: `AdministrationBasePage` (Bootstrap utilities CSS + Rotterdam NLDS-thema).
- `nl.rotterdam.huwelijk.burger_common` — gedeelde basisklassen voor burgerpagina's: `BurgerBasePage`.
- `nl.rotterdam.huwelijk.config` — Spring Boot configuratieklassen.
- `nl.rotterdam.huwelijk.identity` — publiek SPI-contract (adapter-poorten zoals `PersonLookupService`, `BurgerLoginPageMount`, `CurrentUserProvider`) waartegen adapter-modules zoals `remote-local` bouwen. Bewuste uitzondering op de feature-packageconventie hieronder: dit is geen feature maar een cross-cutting contract, gebruikt door meerdere features én door `WicketApplication`/`config`.

Elke feature-package bevat de volgende sub-packages:
- `feature.<naam>.ui` — Wicket-pagina's en UI-hulpklassen (FormDto, componenten).
- `feature.<naam>.application` — Service-interfaces (public) en service-implementaties (package-private).
- `feature.<naam>.domain` — DTOs en domeinklassen (`CreateXxxDto`, `ChangeXxxDto`, `ListXxxDto`, resultaatrecords).
- `feature.<naam>.repository` — Repository-interfaces.

### Service layer
- JPA Entities mogen de service layer niet verlaten.
- De inputs en outputs van de service layer moeten altijd immutable `record` klassen zijn (DTOs).
- Service implementaties mogen intern JPA Entities gebruiken, maar mogen ze **nooit** teruggeven aan of ontvangen van de presentation layer.
- Annoteer alle lees-methoden in een `@Service`-implementatie met `@Transactional(readOnly = true)` en schrijf-/verwijdermethoden met `@Transactional`, zodat de Hibernate-sessie open blijft voor lazy-loaded collecties.
- Splits de DTO's altijd op naar gebruik: gebruik `CreateXxxDto` voor aanmaken (geen id, geen aangemaaktOp), `ChangeXxxDto` voor wijzigen (met id, zonder aangemaaktOp), en `ListXxxDto` voor weergave/overzichten (uitsluitend de velden die in de lijstweergave worden getoond — niet alle entiteitsvelden). Dit zijn aparte `record`-klassen; dupliceer velden gerust.
- **Beheer-service methode conventies:**
    - `create`-methoden retourneren de aangemaakte primaire sleutel als primitief (`long`, niet `Long`).
    - `update`-methoden retourneren `void`.
    - `delete`- en toggle-methoden accepteren de primaire sleutel als primitief (`long id`, niet `Long id`).
    - **Gebruik altijd primitieven** (`long`, `int`, `boolean`, …) in plaats van wrapper-typen (`Long`, `Integer`, `Boolean`, …) waar dat mogelijk is — dit geldt voor methode-parameters én retourtypen in services én repositories.

### Wicket
- Injecteer altijd een service **interface** (niet de implementatie) via `@SpringBean`, zodat Wicket een JDK dynamic proxy kan aanmaken (voorkomt CGLIB-/Objenesis-problemen zonder no-arg constructor).
- Activeer het Rotterdam NLDS-thema via `PatchingNldsRotterdamDesignSystemThemeBehavior.INSTANCE` direct op de page (niet via een `TransparentWebMarkupContainer` op `<html>`), zodat `<wicket:fragment>`-tags vindbaar blijven.
- **`RdFormFieldTextInput` met een niet-`String` modeltype:** wanneer het HTML-inputtype een custom Java-type vereist (bijv. `setHtmlInputType("time")` → `LocalTime`, `setHtmlInputType("date")` → `LocalDate`, `setHtmlInputType("number")` → `Integer`/`BigDecimal`), roep dan altijd `setModelType(Xxx.class)` aan zodat Wicket het type correct kan converteren bij formulier-submit. Voorbeeld: `.setHtmlInputType("time").setModelType(LocalTime.class)`.
- Gebruik voor formulieren een **`FormDto`** klasse (mutable POJO, implementeert `Serializable`) als model object van het `Form`. Maak een `Model<FormDto> model = Model.of(formDto)` aan en gebruik dat als model van het `Form` én als eerste argument voor `LambdaModel.of(model, f -> f.veld, (f, v) -> f.veld = v)` voor elk veld. Gebruik `ListModel` voor `List`-velden (`Model.of(new ArrayList<>())` werkt niet betrouwbaar voor lijsten in Wicket).
- **Formulieren worden OO opgebouwd via een inner class** die `Form<XxxFormDto>` uitbreidt. Gebruik nooit een anonieme klasse of een formulier dat in een variabele wordt gestopt. De inner class:
    - Is `private` en heet `CreateXxxForm` (aanmaken) of `ChangeXxxForm` (wijzigen).
    - Bevat een `onInitialize()`-methode die alle kinderen toevoegt via **één** varargs `add()`-aanroep, zodat de hiërarchie van het formulier direct zichtbaar is in de code.
    - Bevat een `onSubmit()`-methode voor de submit-logica.
    - Heeft toegang tot de `@SpringBean`-service van de omringende page (via de outer class reference).
    - Voorbeeld: `private class ChangeBapsForm extends Form<BapsFormDto> { ... }`
- **FormDto-conventies:**
    - Velden zijn `private` met standaard getters en setters. Gebruik voor `LambdaModel`-bindingen **method references** in plaats van inline lambdas: `LambdaModel.of(model, BapsFormDto::getNaam, BapsFormDto::setNaam)`. In Java kunnen lambdas niet direct naar instantievelden wijzen zoals method references naar methoden kunnen wijzen; getters en setters zijn daarom noodzakelijk voor leesbare bindingen.
    - Gebruik `LocalDate` voor datumvelden (niet `String`). Wicket converteert automatisch via de globaal geregistreerde `LocalDateConverter(DateTimeFormatter.ISO_LOCAL_DATE)` in `WicketApplication`.
    - In de statische `vanDto(XxxDto dto)`-methode worden waarden via setters overgenomen: `form.setVeld(dto.veld())`. Gebruik **nooit** een null-naar-lege-string coercitie zoals `dto.veld() != null ? dto.veld() : ""`. Uitzondering: voor `List`-velden geldt een fallback naar een lege `ArrayList` (`dto.lijst() != null ? new ArrayList<>(dto.lijst()) : new ArrayList<>()`).

### Algemeen
- Gebruik `record` klassen voor DTOs, command- en resultaatobjecten.
- JPA Entity klassen eindigen op `Entity` (bijv. `BapsEntity`) en staan in `nl.rotterdam.huwelijk.persistence`.
- Voeg geen JPA-annotaties toe aan klassen buiten het `persistence`-package.
- Aanmaken en wijzigen mogen nooit op dezelfde pagina staan. Gebruik altijd afzonderlijke pagina's (bijv. `BapsCreatePage` en `BapsUpdatePage`).
- Genereer nooit methoden of klassen die nergens worden aangeroepen of gebruikt.
- Genereer **nooit** `serialVersionUID`-velden. We gebruiken Java-serialisatie niet op deze manier en willen geen achterwaartse compatibiliteit voor geserialiseerde klassen.

### Integratietests
- Gebruik in integratietests altijd **services** om testdata aan te maken, niet repositories of JPA Entities. Entities mogen alleen binnen service-implementaties gebruikt worden.
- Gebruik `@Transactional` op de testklasse zodat elke test in een transactie draait die na afloop wordt teruggedraaid. Hierdoor is geen handmatige cleanup van testdata nodig.
- **Let op bij nested transacties:** wanneer een service-methode `@Transactional(propagation = REQUIRES_NEW)` gebruikt, commit die inner-transactie onafhankelijk en wordt **niet** teruggedraaid door de `@Transactional` op de testklasse. In dat geval moet je de data handmatig opschonen in een `@BeforeEach`-methode via service-methoden (bijv. `delete()`/`deleteAll()`), zodat de database vóór elke test weer in de oorspronkelijke staat is.
- Wanneer seed-data (Flyway) interfereert met de test, verwijder die data via service-methoden in een `@BeforeEach`-methode. Door `@Transactional` wordt ook deze cleanup na de test teruggedraaid.

### Value types
- Gebruik **value types** voor domeinwaarden die validatie, type-veiligheid of semantiek toevoegen (bijv. `PersonFullName` voor een volledige naam).
- Value types implementeren de `ValueHolder<T>` interface met een `T getValue()` methode.
- `toString()` retourneert `SimpleClassName[value]`, bijvoorbeeld `PersonFullName[Jan de Vries]`. Gebruik `getValue()` wanneer de ruwe waarde nodig is (bijv. voor weergave in UI of opslag in database).
- Validatie vindt plaats in de constructor. Gooi specifieke `RuntimeException`-subklassen bij ongeldige input (bijv. `PersonFullNameTooShortException`, `PersonFullNameTooLongException`).
- **JPA-integratie:** maak een `AttributeConverter` aan in het `persistence`-package die de conversie tussen het value type en het databasetype afhandelt. Gebruik `@Convert(converter = ...)` op het entity-veld.
- **Wicket-integratie:** maak een `IConverter` aan in het `ui`-package die de conversie tussen `String` en het value type afhandelt. Vang validatie-excepties af en geef nette Wicket `ConversionException`-meldingen. Registreer de converter globaal in `WicketApplication.newConverterLocator()`.
- Value types worden gebruikt in JPA Entities, DTOs (`CreateXxxDto`, `ChangeXxxDto`, `ListXxxDto`) en `FormDto`-klassen — consistent door alle lagen heen.
- `ValueHolder<T>` staat in `nl.rotterdam.huwelijk.domain` — een gedeeld package, niet in een feature-package — zodat meerdere features het kunnen hergebruiken.
