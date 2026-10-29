package nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Een week (maandag tot en met zondag) waarvoor een trouwlijst gemaakt kan worden.
 */
public record TrouwlijstWeekDto(LocalDate van, LocalDate tot) implements Serializable {
}
