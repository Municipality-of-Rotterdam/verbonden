package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import java.io.Serializable;
import nl.rotterdam.verbonden.core.domain.Postcode;

import java.time.LocalDate;

/**
 * Een getuige zoals de medewerker die in beheer ziet.
 *
 * @param naam de naam zoals het bruidspaar of de medewerker die opgaf
 */
public record DetailGetuigeDto(
        int volgnummer,
        String naam,
        String voornamen,
        String achternaam,
        LocalDate geboortedatum,
        Postcode postcode,
        String woonplaats,
        String land
) implements Serializable {
}
