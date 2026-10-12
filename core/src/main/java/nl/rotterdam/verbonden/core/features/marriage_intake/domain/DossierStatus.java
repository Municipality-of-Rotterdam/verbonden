package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Status van een huwelijksdossier. Een burger kan het dossier alleen wijzigen zolang het de status
 * {@link #CONCEPT} heeft; na het indienen beoordeelt de backoffice de aanvraag.
 * <p>
 * Een burger heeft hoogstens één {@link #ACTIEF actief} dossier. Een {@link #GEANNULEERD geannuleerd} dossier
 * blijft bewaard, maar telt niet meer mee: de burger kan daarna een nieuw dossier aanmaken. Een afgewezen dossier
 * blijft actief, zodat de burger de afwijzing ziet; wil het paar opnieuw beginnen, dan annuleert de backoffice het.
 */
public enum DossierStatus {

    CONCEPT("Concept"),
    INGEDIEND("Ingediend"),
    GEACCEPTEERD("Geaccepteerd"),
    AFGEWEZEN("Afgewezen"),
    GEANNULEERD("Geannuleerd");

    public static final Set<DossierStatus> ACTIEF = EnumSet.complementOf(EnumSet.of(GEANNULEERD));

    private final String label;

    DossierStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isActief() {
        return ACTIEF.contains(this);
    }
}
