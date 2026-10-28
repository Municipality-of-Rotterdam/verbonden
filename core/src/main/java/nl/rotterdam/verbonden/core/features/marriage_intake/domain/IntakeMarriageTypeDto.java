package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record IntakeMarriageTypeDto(
        CeremonieSoort soort,
        String titel,
        Bedrag prijs,
        String prijsPrefix,
        List<String> bulletPoints,
        LocalDate eersteGelegenheid,
        boolean active,
        Long locatieId,
        String locatieNaam
) implements Serializable {
}
