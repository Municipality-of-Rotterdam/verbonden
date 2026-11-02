package nl.rotterdam.verbonden.core.features.marriage_type_administration.domain;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.domain.Bedrag;

public record CreateMarriageTypeDto(
        CeremonieSoort soort,
        String titel,
        String tekst,
        Bedrag prijs,
        String url,
        Long locatieId,
        boolean active
) {
}
