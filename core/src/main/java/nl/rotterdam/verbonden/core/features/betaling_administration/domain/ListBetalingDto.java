package nl.rotterdam.verbonden.core.features.betaling_administration.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStatus;
import nl.rotterdam.verbonden.core.online_betalen.BetaalSoort;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Eén betaalpoging, voor de overzichten in beheer.
 *
 * @param omschrijving      de omschrijving van de betaalmutatie, of {@code null} bij de betaling van de aanvraag
 * @param afgerondOp        wanneer de uitslag binnenkwam, of {@code null} zolang die er niet is
 * @param externeReferentie het kenmerk bij de betaalprovider, of {@code null} als de betaling daar niet aankwam
 */
public record ListBetalingDto(
        UUID dossierId,
        String betalingskenmerk,
        BetaalSoort soort,
        String omschrijving,
        Bedrag bedrag,
        BetalingStatus status,
        BurgerServiceNummer betalerBsn,
        LocalDateTime gestartOp,
        LocalDateTime afgerondOp,
        String externeReferentie
) implements Serializable {

    /** Waarvoor betaald is, voor de weergave. */
    public String waarvoor() {
        return omschrijving != null ? omschrijving : "Aanvraag";
    }
}
