package nl.rotterdam.verbonden.features.location_administration.domain;

import java.io.Serializable;

public record ListLocatieDto(
        long id,
        String naam,
        String fotoUrl,
        String omschrijving,
        String detailUrl
) implements Serializable {
}
