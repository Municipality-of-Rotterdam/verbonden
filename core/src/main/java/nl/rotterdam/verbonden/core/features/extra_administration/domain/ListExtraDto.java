package nl.rotterdam.verbonden.core.features.extra_administration.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ListExtraDto(
        long id,
        String naam,
        BigDecimal prijs,
        LocalDate startdatum,
        LocalDate einddatum
) implements Serializable {
}
