package nl.rotterdam.huwelijk.features.location_administration.domain;

import java.io.Serializable;

public record ListLocatieDto(
        long id,
        String naam,
        String fotoUrl
) implements Serializable {
}
