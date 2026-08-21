package nl.rotterdam.verbonden.features.extra_administration.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ListExtraDto(
        long id,
        ExtraType type,
        String naam,
        BigDecimal prijs,
        LocalDate startdatum,
        LocalDate einddatum
) implements Serializable {
}
