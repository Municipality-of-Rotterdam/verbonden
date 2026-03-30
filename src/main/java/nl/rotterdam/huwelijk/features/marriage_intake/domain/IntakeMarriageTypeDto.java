package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import java.math.BigDecimal;
import java.util.List;

public record IntakeMarriageTypeDto(
        CeremonieSoort soort,
        String titel,
        BigDecimal prijs,
        List<String> bulletPoints,
        boolean active
) {
}
