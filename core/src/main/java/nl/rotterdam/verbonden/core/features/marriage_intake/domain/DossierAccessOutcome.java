package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

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
        /** BSN already linked to the requested dossier. */
        GRANTED,
        /**
         * BSN has no dossier yet and the requested dossier still has room for a second partner:
         * the BSN may join, but only after explicitly accepting (see
         * {@code MarriageIntakeService#acceptInvitation}). {@code dossierId} is the requested dossier.
         */
        INVITED,
        /** BSN has its own (different) dossier; {@code dossierId} refers to that dossier. */
        SWITCHED_DOSSIER,
        /** Requested dossier does not exist, or already has two BSNs and does not include this BSN. */
        NOT_AUTHORIZED
    }
}
