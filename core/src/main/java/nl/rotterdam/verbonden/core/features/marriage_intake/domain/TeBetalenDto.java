package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.io.Serializable;

/**
 * Een betaalmutatie die de burger nog moet betalen.
 *
 * @param inBehandeling of er al een betaling voor loopt waarvan de bank de uitslag nog niet heeft gemeld; dan niet
 *                      nog eens laten betalen
 */
public record TeBetalenDto(long betaalmutatieId, String omschrijving, Bedrag bedrag, boolean inBehandeling)
        implements Serializable {
}
