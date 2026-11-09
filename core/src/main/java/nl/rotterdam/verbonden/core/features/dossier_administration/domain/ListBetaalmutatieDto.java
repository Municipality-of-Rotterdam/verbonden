package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @param betaald       of er een gelukte betaling voor de mutatie is
 * @param verwijderbaar of de mutatie nog weg mag: alleen zolang er geen betaling voor is gestart
 * @param prijswijziging of Verbonden de mutatie aanmaakte omdat het dossier duurder werd, of een medewerker hem
 *                      handmatig toevoegde
 */
public record ListBetaalmutatieDto(
        long id,
        String omschrijving,
        Bedrag bedrag,
        boolean betaald,
        boolean verwijderbaar,
        boolean prijswijziging,
        String aangemaaktDoor,
        LocalDateTime aangemaaktOp
) implements Serializable {
}
