# Copilot Instructions

## Architectuurregels

### Service layer
- JPA Entities mogen de service layer niet verlaten.
- De inputs en outputs van de service layer moeten altijd immutable `record` klassen zijn (DTOs).
- Service implementaties mogen intern JPA Entities gebruiken, maar mogen ze **nooit** teruggeven aan of ontvangen van de presentation layer.

### Wicket
- Injecteer altijd een service **interface** (niet de implementatie) via `@SpringBean`, zodat Wicket een JDK dynamic proxy kan aanmaken (voorkomt CGLIB-/Objenesis-problemen zonder no-arg constructor).
- Activeer het Rotterdam NLDS-thema via `PatchingNldsRotterdamDesignSystemThemeBehavior.INSTANCE` direct op de page (niet via een `TransparentWebMarkupContainer` op `<html>`), zodat `<wicket:fragment>`-tags vindbaar blijven.

### Algemeen
- Gebruik `record` klassen voor DTOs, command- en resultaatobjecten.
- Voeg geen JPA-annotaties toe aan klassen buiten het `baps`-package (of gelijkwaardige datapakketten).
