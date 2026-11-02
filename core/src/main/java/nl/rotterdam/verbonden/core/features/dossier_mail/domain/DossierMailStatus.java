package nl.rotterdam.verbonden.core.features.dossier_mail.domain;

/**
 * Waar een e-mail in het verzendproces staat.
 */
public enum DossierMailStatus {

    /** Nog niet verstuurd; de volgende poging staat gepland. */
    IN_WACHT("In wacht"),

    VERZONDEN("Verzonden"),

    /** Verzenden is mislukt en er volgt geen nieuwe poging meer. */
    MISLUKT("Mislukt");

    private final String label;

    DossierMailStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
