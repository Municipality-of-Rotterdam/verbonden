package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import java.math.BigDecimal;

public record ExtraDto(
        long id,
        String naam,
        String omschrijving,
        String afbeelding,
        BigDecimal prijs
) {
}
