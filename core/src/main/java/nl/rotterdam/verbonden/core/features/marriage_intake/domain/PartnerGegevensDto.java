package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;

import java.io.Serializable;
import java.time.LocalDate;

public record PartnerGegevensDto(
        BurgerServiceNummer bsn,
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
