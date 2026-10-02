package nl.rotterdam.verbonden.core.burger_common;

/**
 * Foutpagina die Wicket toont bij een onafgevangen exceptie. Wicket logt de exceptie zelf.
 */
public class BurgerInternalErrorPage extends BurgerErrorPage {

    public BurgerInternalErrorPage() {
        super(500, "Er is een interne fout opgetreden");
    }
}
