package nl.rotterdam.verbonden.core.features.marriage_type_administration.domain;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.io.Serializable;

public record ListMarriageTypeDto(
        long id,
        CeremonieSoort soort,
        String titel,
        Bedrag prijs,
        String locatieNaam,
        boolean active
) implements Serializable {
}
