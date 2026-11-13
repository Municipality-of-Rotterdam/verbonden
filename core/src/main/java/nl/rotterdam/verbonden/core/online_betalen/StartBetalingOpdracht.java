package nl.rotterdam.verbonden.core.online_betalen;

import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;

import java.util.UUID;

/**
 * Alles wat een adapter nodig heeft om een betaling te starten.
 *
 * @param dossierId        het huwelijksdossier waarvoor betaald wordt
 * @param soort            de eerste betaling van het dossier, of een betaling voor een latere wijziging
 * @param betalingskenmerk uniek kenmerk van deze betaalpoging; de adapter meldt de uitslag onder dit kenmerk
 *                         terug. Alleen cijfers, hoogstens {@link #MAX_LENGTE_BETALINGSKENMERK} tekens.
 * @param bedrag           het te betalen bedrag, groter dan nul
 * @param registratieType  huwelijk of geregistreerd partnerschap
 * @param betalerBsn       de ingelogde partner die betaalt
 * @param betalerNaam      de naam van de betaler
 * @param retourUrl        absolute URL waar de burger na het betalen terugkomt
 */
public record StartBetalingOpdracht(
        UUID dossierId,
        BetaalSoort soort,
        String betalingskenmerk,
        Bedrag bedrag,
        RegistratieType registratieType,
        BurgerServiceNummer betalerBsn,
        String betalerNaam,
        String retourUrl) {

    public static final int MAX_LENGTE_BETALINGSKENMERK = 30;
}
