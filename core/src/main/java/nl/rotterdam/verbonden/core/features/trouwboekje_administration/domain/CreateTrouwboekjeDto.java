package nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.time.LocalDate;

public record CreateTrouwboekjeDto(
        String naam,
        String omschrijving,
        String afbeelding,
        Bedrag prijs,
        LocalDate startdatum,
        LocalDate einddatum
) {
}
