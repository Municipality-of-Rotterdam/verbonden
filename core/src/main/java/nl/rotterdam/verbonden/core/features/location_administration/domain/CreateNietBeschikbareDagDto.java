package nl.rotterdam.verbonden.core.features.location_administration.domain;

import java.time.LocalDate;

public record CreateNietBeschikbareDagDto(
        long locatieId,
        LocalDate datum,
        String reden
) {
}
