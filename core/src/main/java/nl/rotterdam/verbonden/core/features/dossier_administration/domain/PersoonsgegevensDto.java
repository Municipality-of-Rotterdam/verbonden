package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Persoonsgegevens van een partner zonder BSN, zoals de medewerker ze van het paspoort overneemt.
 */
public record PersoonsgegevensDto(
        String achternaam,
        String voornamen,
        LocalDate geboortedatum,
        String geboorteplaats,
        String nationaliteit,
        String burgerlijkeStaat
) implements Serializable {
}
