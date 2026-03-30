package nl.rotterdam.huwelijk.features.marriage_type_administration.domain;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;

import java.math.BigDecimal;

public record ChangeMarriageTypeDto(
        long id,
        CeremonieSoort soort,
        String titel,
        String tekst,
        BigDecimal prijs,
        String url
) {
}
