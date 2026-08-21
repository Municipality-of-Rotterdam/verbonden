package nl.rotterdam.huwelijk.features.marriage_type_administration.domain;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;

import java.io.Serializable;
import java.math.BigDecimal;

public record ListMarriageTypeDto(
        long id,
        CeremonieSoort soort,
        String titel,
        BigDecimal prijs,
        String locatieNaam,
        boolean active
) implements Serializable {
}
