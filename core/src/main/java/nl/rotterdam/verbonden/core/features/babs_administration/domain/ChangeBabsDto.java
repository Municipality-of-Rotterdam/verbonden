package nl.rotterdam.verbonden.core.features.babs_administration.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record ChangeBabsDto(
        Long id,
        PersonFullName naam,
        String fotoUrl,
        String detailUrl,
        boolean actief,
        LocalDate actiefVanaf,
        LocalDate actiefTotEnMet,
        List<DayOfWeek> beschikbareDagen
) {
}
