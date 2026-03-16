package nl.rotterdam.huwelijk.features.marriage_type_administration.domain;

import nl.rotterdam.huwelijk.domain.MarriageType;

import java.math.BigDecimal;

public record ChangeMarriageTypeDto(
        long id,
        MarriageType soort,
        String titel,
        String tekst,
        BigDecimal prijs,
        String url
) {
}
