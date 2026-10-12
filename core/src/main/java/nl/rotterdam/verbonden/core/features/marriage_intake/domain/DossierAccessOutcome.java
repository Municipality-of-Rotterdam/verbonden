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
         * The requested dossier is a {@code CONCEPT} with room for a second partner, and the BSN has no active
         * dossier, or only a {@code CONCEPT} of its own without a second partner: the BSN may join, but only
         * after explicitly accepting (see {@code MarriageIntakeService#acceptInvitation}), which cancels that
         * own concept. {@code dossierId} is the requested dossier.
         */
        INVITED,
        /**
         * The BSN was invited to the requested dossier, but its own active dossier is already submitted or has a
         * second partner. Only the backoffice can cancel that dossier, so the invitation cannot be accepted;
         * {@code dossierId} refers to the own dossier.
         */
        INVITATION_BLOCKED,
        /** BSN has its own (different) active dossier; {@code dossierId} refers to that dossier. */
        SWITCHED_DOSSIER,
        /**
         * Requested dossier does not exist, is no longer active, or already has two BSNs and does not include
         * this BSN.
         */
        NOT_AUTHORIZED
    }
}
