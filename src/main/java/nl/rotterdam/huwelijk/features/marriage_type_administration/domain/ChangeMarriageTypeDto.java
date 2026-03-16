package nl.rotterdam.huwelijk.features.marriage_type_administration.domain;

import java.math.BigDecimal;

public record ChangeMarriageTypeDto(
        long id,
        String titel,
        String tekst,
        BigDecimal prijs,
        String url
) {
}
