package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public record TrouwboekjeKeuzeDto(
        long id,
        String naam,
        String omschrijving,
        String afbeelding,
        BigDecimal prijs
) implements Serializable {
}
