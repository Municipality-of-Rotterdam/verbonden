package nl.rotterdam.huwelijk.features.marriage_type_administration.domain;

import nl.rotterdam.huwelijk.domain.MarriageType;

import java.io.Serializable;
import java.math.BigDecimal;

public record ListMarriageTypeDto(
        long id,
        MarriageType soort,
        String titel,
        BigDecimal prijs
) implements Serializable {
}
