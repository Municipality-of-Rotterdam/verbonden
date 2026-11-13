package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.online_betalen.BetaalSoort;

/**
 * Soort en stand van een betaalpoging.
 */
public record BetalingStandDto(BetaalSoort soort, BetalingStatus status) {
}
