package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record IntakeMarriageTypeDto(
        CeremonieSoort soort,
        String titel,
        BigDecimal prijs,
        String prijsPrefix,
        List<String> bulletPoints,
        LocalDate eersteGelegenheid,
        boolean active
) {
}
