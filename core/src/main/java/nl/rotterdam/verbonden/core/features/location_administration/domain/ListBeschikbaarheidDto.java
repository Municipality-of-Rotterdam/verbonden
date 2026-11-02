package nl.rotterdam.verbonden.core.features.location_administration.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public record ListBeschikbaarheidDto(
        long id,
        HuwelijksType huwelijkstype,
        DayOfWeek dagVanDeWeek,
        LocalTime startTijd,
        LocalTime eindTijd,
        int duurInMinuten,
        Bedrag prijs,
        LocalDate ingangsdatum,
        LocalDate einddatum
) implements Serializable {
}
