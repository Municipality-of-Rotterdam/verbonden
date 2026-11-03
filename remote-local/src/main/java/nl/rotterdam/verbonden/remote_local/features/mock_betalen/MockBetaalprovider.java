package nl.rotterdam.verbonden.remote_local.features.mock_betalen;

import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslag;

import java.time.Duration;

/**
 * De kant van de mock-betaalprovider die de mock-betaalpagina gebruikt. Een interface, zodat de pagina hem via
 * {@code @SpringBean} kan krijgen.
 */
public interface MockBetaalprovider {

    /**
     * Meldt de uitslag aan Verbonden, net als een echte betaalprovider dat los van de browser doet.
     *
     * @param vertraging meld de uitslag pas na deze tijd, zoals een bank die nog even nadenkt; {@link Duration#ZERO}
     *                   voor meteen
     * @return de retour-URL waar de burger heen moet
     */
    String rondBetalingAf(String betalingskenmerk, BetaalUitslag uitslag, Duration vertraging);
}
