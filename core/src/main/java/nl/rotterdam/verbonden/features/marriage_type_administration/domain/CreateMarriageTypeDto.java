package nl.rotterdam.verbonden.features.marriage_type_administration.domain;

import nl.rotterdam.verbonden.features.marriage_intake.domain.CeremonieSoort;

import java.math.BigDecimal;

public record CreateMarriageTypeDto(
        CeremonieSoort soort,
        String titel,
        String tekst,
        BigDecimal prijs,
        String url,
        Long locatieId,
        boolean active
) {
}
