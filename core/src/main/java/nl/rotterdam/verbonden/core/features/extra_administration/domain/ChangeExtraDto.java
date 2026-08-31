package nl.rotterdam.verbonden.core.features.extra_administration.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChangeExtraDto(
        long id,
        ExtraType type,
        String naam,
        String omschrijving,
        String afbeelding,
        BigDecimal prijs,
        LocalDate startdatum,
        LocalDate einddatum
) {
}
