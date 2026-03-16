package nl.rotterdam.huwelijk.features.baps_administration.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ListBapsDto(
        Long id,
        PersonFullName naam,
        boolean actief,
        LocalDate actiefVanaf,
        LocalDate actiefTotEnMet,
        LocalDateTime aangemaaktOp
) implements Serializable {
}
