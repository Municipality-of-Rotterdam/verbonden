package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

/**
 * Status van een huwelijksdossier. Een burger kan het dossier alleen wijzigen zolang het de status
 * {@link #CONCEPT} heeft; na het indienen beoordeelt de backoffice de aanvraag.
 */
public enum DossierStatus {

    CONCEPT("Concept"),
    INGEDIEND("Ingediend"),
    GEACCEPTEERD("Geaccepteerd"),
    AFGEWEZEN("Afgewezen");

    private final String label;

    DossierStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
