package nl.rotterdam.verbonden.core.administration_common;

/**
 * Foutpagina die Wicket in een {@code /beheer}-request toont bij een onafgevangen exceptie. Wicket logt de exceptie zelf.
 */
public class AdministrationInternalErrorPage extends AdministrationErrorPage {

    public AdministrationInternalErrorPage() {
        super(500, "Er is een interne fout opgetreden");
    }
}
