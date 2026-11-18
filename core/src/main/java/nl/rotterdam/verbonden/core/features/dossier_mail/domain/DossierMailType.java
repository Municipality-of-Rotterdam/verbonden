package nl.rotterdam.verbonden.core.features.dossier_mail.domain;

/**
 * Soort e-mail. Bij elk type hoort een template in {@code resources/mail-templates}.
 */
public enum DossierMailType {

    BEVESTIGING_INDIENING("Bevestiging indienen aanvraag");

    private final String label;

    DossierMailType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
