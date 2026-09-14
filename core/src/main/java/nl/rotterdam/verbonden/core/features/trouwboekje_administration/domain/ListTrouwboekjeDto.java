package nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ListTrouwboekjeDto(
        long id,
        String naam,
        BigDecimal prijs,
        LocalDate startdatum,
        LocalDate einddatum
) implements Serializable {
}
