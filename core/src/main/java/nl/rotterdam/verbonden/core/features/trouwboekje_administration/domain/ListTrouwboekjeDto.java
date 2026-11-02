package nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.io.Serializable;
import java.time.LocalDate;

public record ListTrouwboekjeDto(
        long id,
        String naam,
        String afbeelding,
        Bedrag prijs,
        LocalDate startdatum,
        LocalDate einddatum
) implements Serializable {
}
