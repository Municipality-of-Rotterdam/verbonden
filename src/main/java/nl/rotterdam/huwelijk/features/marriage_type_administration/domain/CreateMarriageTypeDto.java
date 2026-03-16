package nl.rotterdam.huwelijk.features.marriage_type_administration.domain;

import java.math.BigDecimal;

public record CreateMarriageTypeDto(
        String titel,
        String tekst,
        BigDecimal prijs,
        String url
) {
}
