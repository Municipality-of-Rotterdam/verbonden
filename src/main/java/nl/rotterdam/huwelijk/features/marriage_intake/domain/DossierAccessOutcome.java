package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import java.util.UUID;

/**
 * Represents the outcome of checking and granting access to a dossier for a given BSN.
 *
 * @param scenario  the access scenario that applies
 * @param dossierId the dossier UUID to use, or {@code null} when {@code scenario}
 *                  is {@link Scenario#NOT_AUTHORIZED}
 */
public record DossierAccessOutcome(Scenario scenario, UUID dossierId) {

    public enum Scenario {
        /** BSN already linked to the requested dossier, or just registered as bsn2. */
        GRANTED,
        /** BSN has its own (different) dossier; {@code dossierId} refers to that dossier. */
        SWITCHED_DOSSIER,
        /** Requested dossier already has two BSNs and does not include this BSN. */
        NOT_AUTHORIZED
    }
}
