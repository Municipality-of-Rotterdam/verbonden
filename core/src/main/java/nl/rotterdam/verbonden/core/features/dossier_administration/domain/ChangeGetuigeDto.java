package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.domain.Postcode;

import java.time.LocalDate;
import java.time.Period;

/**
 * De gegevens van een getuige, zoals de medewerker ze in beheer opslaat.
 */
public record ChangeGetuigeDto(
        int volgnummer,
        String naam,
        String voornamen,
        String achternaam,
        LocalDate geboortedatum,
        Postcode postcode,
        String woonplaats,
        String land
) {

    /** Het land dat de medewerker bij een nieuwe getuige voorgevuld ziet. */
    public static final String NEDERLAND = "Nederland";

    /** Een getuige moet op de dag van de ceremonie minstens deze leeftijd hebben. */
    public static final int MINIMUM_LEEFTIJD = 18;

    /**
     * Of de getuige op de gegeven datum jonger is dan {@link #MINIMUM_LEEFTIJD}; {@code false} als de
     * geboortedatum onbekend is. Wie op 29 februari is geboren, wordt in een gewoon jaar op 1 maart een jaar ouder.
     */
    public boolean isTeJongOp(LocalDate datum) {
        return geboortedatum != null && Period.between(geboortedatum, datum).getYears() < MINIMUM_LEEFTIJD;
    }

    /**
     * Of de getuige in Nederland woont maar een postcode heeft die geen Nederlandse postcode is.
     */
    public boolean heeftOngeldigeNederlandsePostcode() {
        return land != null && land.strip().equalsIgnoreCase(NEDERLAND) && postcode != null
                && !postcode.isNederlands();
    }

    /**
     * Of er voor deze getuige niets is ingevuld. Het land telt niet mee: dat is standaard al ingevuld.
     */
    public boolean isLeeg() {
        return isLeeg(naam) && isLeeg(voornamen) && isLeeg(achternaam) && geboortedatum == null
                && postcode == null && isLeeg(woonplaats);
    }

    private static boolean isLeeg(String waarde) {
        return waarde == null || waarde.isBlank();
    }
}
