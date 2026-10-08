package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.BuitenlandsPersoonsnummer;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @param volgorde                  1 of 2; identificeert de partner binnen het dossier
 * @param bsn                       {@code null} voor een partner zonder BSN
 * @param buitenlandsPersoonsnummer alleen voor een partner zonder BSN: het persoonsnummer uit het paspoort
 */
public record PartnerGegevensDto(
        int volgorde,
        BurgerServiceNummer bsn,
        BuitenlandsPersoonsnummer buitenlandsPersoonsnummer,
        String achternaam,
        String voornamen,
        LocalDate geboortedatum,
        String geboorteplaats,
        String nationaliteit,
        String burgerlijkeStaat,
        Telefoonnummer telefoonnummer,
        Emailadres emailadres,
        String gekozenAchternaam,
        long versie
) implements Serializable {
}
