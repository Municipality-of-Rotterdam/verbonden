package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import java.io.Serializable;

public record GetuigeDto(
        int volgnummer,
        String naam
) implements Serializable {
}
