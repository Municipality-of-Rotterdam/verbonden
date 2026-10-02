package nl.rotterdam.verbonden.core.burger_common;

/**
 * Foutpagina die Wicket toont wanneer een pagina niet meer in de sessie staat, bijv. na een verlopen sessie.
 */
public class BurgerPageExpiredPage extends BurgerErrorPage {

    public BurgerPageExpiredPage() {
        super(410, "De pagina is verlopen. Ga terug naar de beginpagina en probeer het opnieuw.");
    }
}
