package nl.rotterdam.huwelijk.identity;

import java.time.LocalDate;

public record PersonInfo(
        String achternaam,
        String voornamen,
        LocalDate geboortedatum,
        String geboorteplaats,
        String nationaliteit,
        String burgerlijkeStaat
) {
}
