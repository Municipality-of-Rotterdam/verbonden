# Copilot Instructions

## Architectuurregels

### Service layer
- JPA Entities mogen de service layer niet verlaten.
- De inputs en outputs van de service layer moeten altijd immutable `record` klassen zijn (DTOs).
- Service implementaties mogen intern JPA Entities gebruiken, maar mogen ze **nooit** teruggeven aan of ontvangen van de presentation layer.
- Annoteer alle lees-methoden in een `@Service`-implementatie met `@Transactional(readOnly = true)` en schrijf-/verwijdermethoden met `@Transactional`, zodat de Hibernate-sessie open blijft voor lazy-loaded collecties.
- Splits de DTO's altijd op naar gebruik: gebruik `CreateXxxDto` voor aanmaken (geen id, geen aangemaaktOp), `ChangeXxxDto` voor wijzigen (met id, zonder aangemaaktOp), en `ListXxxDto` voor weergave/overzichten (alle velden inclusief aangemaaktOp). Dit zijn aparte `record`-klassen; dupliceer velden gerust.

### Wicket
- Injecteer altijd een service **interface** (niet de implementatie) via `@SpringBean`, zodat Wicket een JDK dynamic proxy kan aanmaken (voorkomt CGLIB-/Objenesis-problemen zonder no-arg constructor).
- Activeer het Rotterdam NLDS-thema via `PatchingNldsRotterdamDesignSystemThemeBehavior.INSTANCE` direct op de page (niet via een `TransparentWebMarkupContainer` op `<html>`), zodat `<wicket:fragment>`-tags vindbaar blijven.
- Gebruik voor formulieren een **`FormDto`** klasse (mutable POJO, implementeert `Serializable`) als model object van het `Form`. Maak een `Model<FormDto> model = Model.of(formDto)` aan en gebruik dat als model van het `Form` én als eerste argument voor `LambdaModel.of(model, Getter::get, Setter::set)` voor elk veld. Gebruik `ListModel` voor `List`-velden (`Model.of(new ArrayList<>())` werkt niet betrouwbaar voor lijsten in Wicket).

### Algemeen
- Gebruik `record` klassen voor DTOs, command- en resultaatobjecten.
- JPA Entity klassen eindigen op `Entity` (bijv. `BapsEntity`).
- Voeg geen JPA-annotaties toe aan klassen buiten het `baps`-package (of gelijkwaardige datapakketten).
- Aanmaken en wijzigen mogen nooit op dezelfde pagina staan. Gebruik altijd afzonderlijke pagina's (bijv. `BapsToevoegenPage` en `BapsWijzigenPage`).
- Genereer nooit methoden of klassen die nergens worden aangeroepen of gebruikt.
- Genereer **nooit** `serialVersionUID`-velden. We gebruiken Java-serialisatie niet op deze manier en willen geen achterwaartse compatibiliteit voor geserialiseerde klassen.
