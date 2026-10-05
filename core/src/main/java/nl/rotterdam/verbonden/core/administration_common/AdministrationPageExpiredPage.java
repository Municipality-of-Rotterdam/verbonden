package nl.rotterdam.verbonden.core.administration_common;

/**
 * Foutpagina die Wicket in een {@code /beheer}-request toont wanneer een pagina niet meer in de sessie staat.
 */
public class AdministrationPageExpiredPage extends AdministrationErrorPage {

    public AdministrationPageExpiredPage() {
        super(410, "De pagina is verlopen. Ga terug naar beheer en probeer het opnieuw.");
    }
}
