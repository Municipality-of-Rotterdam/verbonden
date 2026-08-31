package nl.rotterdam.verbonden.core.features.location_administration.domain;

import java.io.Serializable;
import java.math.BigDecimal;
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
        BigDecimal prijs,
        LocalDate ingangsdatum,
        LocalDate einddatum
) implements Serializable {
}
