package nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChangeTrouwboekjeDto(
        long id,
        String naam,
        String omschrijving,
        String afbeelding,
        BigDecimal prijs,
        LocalDate startdatum,
        LocalDate einddatum
) {
}
