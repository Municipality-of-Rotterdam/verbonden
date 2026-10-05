package nl.rotterdam.verbonden.core.administration_common;

/**
 * Foutpagina die Wicket in een {@code /beheer}-request toont wanneer de medewerker geen toegang heeft tot een pagina of actie.
 */
public class AdministrationAccessDeniedPage extends AdministrationErrorPage {

    public AdministrationAccessDeniedPage() {
        super(403, "Toegang geweigerd");
    }
}
