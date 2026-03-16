package nl.rotterdam.huwelijk.features.marriage_type_administration.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public record ListMarriageTypeDto(
        long id,
        String titel,
        BigDecimal prijs
) implements Serializable {
}
