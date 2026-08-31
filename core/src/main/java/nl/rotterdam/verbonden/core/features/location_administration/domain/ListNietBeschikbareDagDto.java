package nl.rotterdam.verbonden.core.features.location_administration.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ListNietBeschikbareDagDto(
        long id,
        LocalDate datum,
        String reden,
        LocalDateTime laatsteWijzigDatum,
        String userid
) implements Serializable {
}
