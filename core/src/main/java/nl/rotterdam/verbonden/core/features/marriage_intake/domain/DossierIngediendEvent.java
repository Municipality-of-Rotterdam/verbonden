package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import java.util.UUID;

/**
 * Een dossier is ingediend. Wordt gepubliceerd binnen de transactie waarin het dossier de status
 * {@link DossierStatus#INGEDIEND} krijgt.
 */
public record DossierIngediendEvent(UUID dossierId) {
}
