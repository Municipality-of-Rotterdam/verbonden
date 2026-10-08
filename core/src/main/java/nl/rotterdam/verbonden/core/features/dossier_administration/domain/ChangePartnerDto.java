package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.domain.BuitenlandsPersoonsnummer;
import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;

/**
 * @param volgorde                  1 of 2
 * @param versie                    de versie van de contactgegevens waarop de wijziging is gebaseerd
 * @param buitenlandsPersoonsnummer alleen voor een partner zonder BSN; anders {@code null}
 * @param persoonsgegevens          alleen voor een partner zonder BSN; anders {@code null}, want die komen uit de BRP
 */
public record ChangePartnerDto(
        int volgorde,
        long versie,
        String gekozenAchternaam,
        Telefoonnummer telefoonnummer,
        Emailadres emailadres,
        BuitenlandsPersoonsnummer buitenlandsPersoonsnummer,
        PersoonsgegevensDto persoonsgegevens
) {
}
