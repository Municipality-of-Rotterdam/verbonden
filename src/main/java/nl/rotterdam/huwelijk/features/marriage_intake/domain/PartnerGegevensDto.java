package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import java.io.Serializable;
import java.time.LocalDate;

public record PartnerGegevensDto(
        String bsn,
        String achternaam,
        String voornamen,
        LocalDate geboortedatum,
        String geboorteplaats,
        String nationaliteit,
        String burgerlijkeStaat,
        Telefoonnummer telefoonnummer,
        Emailadres emailadres,
        String gekozenAchternaam
) implements Serializable {
}
