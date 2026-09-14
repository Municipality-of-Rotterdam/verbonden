package nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTrouwboekjeDto(
        String naam,
        String omschrijving,
        String afbeelding,
        BigDecimal prijs,
        LocalDate startdatum,
        LocalDate einddatum
) {
}
