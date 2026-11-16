package nl.rotterdam.verbonden.core.online_betalen;

import nl.rotterdam.verbonden.core.domain.Bedrag;

/**
 * De uitslag van een betaling zoals de betaalprovider die meldt.
 *
 * @param betalingskenmerk  het kenmerk uit {@link StartBetalingOpdracht#betalingskenmerk()}
 * @param externeReferentie het kenmerk bij de betaalprovider, als die het meestuurt; wijkt het af van
 *                          {@link BetalingGestart#externeReferentie()}, dan wordt de melding genegeerd
 * @param bedrag            het betaalde bedrag, als de betaalprovider het meestuurt; wijkt een gelukte betaling af
 *                          van het gevraagde bedrag, dan wordt de melding genegeerd
 */
public record BetaalUitslagMelding(String betalingskenmerk, String externeReferentie, Bedrag bedrag,
                                   BetaalUitslag uitslag) {
}
