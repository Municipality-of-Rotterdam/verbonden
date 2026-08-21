package nl.rotterdam.verbonden.features.extra_administration.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateExtraDto(
        ExtraType type,
        String naam,
        String omschrijving,
        String afbeelding,
        BigDecimal prijs,
        LocalDate startdatum,
        LocalDate einddatum
) {
}
