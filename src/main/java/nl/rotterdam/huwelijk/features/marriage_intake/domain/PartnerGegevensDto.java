package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import java.io.Serializable;
import java.time.LocalDate;

public record PartnerGegevensDto(
        String achternaam,
        String voornamen,
        LocalDate geboortedatum,
        String geboorteplaats,
        String nationaliteit,
        String burgerlijkeStaat,
        String telefoonnummer,
        String emailadres
) implements Serializable {
}
