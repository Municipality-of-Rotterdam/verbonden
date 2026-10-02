package nl.rotterdam.verbonden.core.burger_common;

/**
 * Foutpagina die Wicket toont wanneer de gebruiker geen toegang heeft tot een pagina of actie.
 */
public class BurgerAccessDeniedPage extends BurgerErrorPage {

    public BurgerAccessDeniedPage() {
        super(403, "Toegang geweigerd");
    }
}
