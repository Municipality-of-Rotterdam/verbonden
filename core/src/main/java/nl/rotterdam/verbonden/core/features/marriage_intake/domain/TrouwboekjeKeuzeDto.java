package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.io.Serializable;

public record TrouwboekjeKeuzeDto(
        long id,
        String naam,
        String omschrijving,
        String afbeelding,
        Bedrag prijs
) implements Serializable {
}
