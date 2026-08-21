package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public record ExtraDto(
        long id,
        String naam,
        String omschrijving,
        String afbeelding,
        BigDecimal prijs
) implements Serializable {
}
