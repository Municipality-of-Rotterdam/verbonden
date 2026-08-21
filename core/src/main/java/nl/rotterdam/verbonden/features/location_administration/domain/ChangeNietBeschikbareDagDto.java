package nl.rotterdam.verbonden.features.location_administration.domain;

import java.time.LocalDate;

public record ChangeNietBeschikbareDagDto(
        long id,
        LocalDate datum,
        String reden
) {
}
