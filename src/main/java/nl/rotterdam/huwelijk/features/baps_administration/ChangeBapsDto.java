package nl.rotterdam.huwelijk.features.baps_administration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record ChangeBapsDto(
        Long id,
        String naam,
        String fotoUrl,
        String hobbies,
        String beschrijving,
        boolean actief,
        LocalDate actiefVanaf,
        LocalDate actiefTotEnMet,
        List<DayOfWeek> beschikbareDagen
) {
}
