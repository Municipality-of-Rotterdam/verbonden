package nl.rotterdam.verbonden.features.marriage_intake.domain;

import java.io.Serializable;

public record GetuigeDto(
        int volgnummer,
        String naam
) implements Serializable {
}
