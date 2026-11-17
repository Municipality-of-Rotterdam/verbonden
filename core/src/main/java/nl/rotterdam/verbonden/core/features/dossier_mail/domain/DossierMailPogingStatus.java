package nl.rotterdam.verbonden.core.features.dossier_mail.domain;

/**
 * Uitkomst van één verzendpoging.
 */
public enum DossierMailPogingStatus {

    GESLAAGD("Geslaagd"),
    MISLUKT("Mislukt");

    private final String label;

    DossierMailPogingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
