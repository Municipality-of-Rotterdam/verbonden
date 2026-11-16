package nl.rotterdam.verbonden.core.online_betalen;

/**
 * Start een online betaling bij een betaalprovider. De implementatie komt uit een adapter-module: een mock voor
 * lokale ontwikkeling, of een koppeling met een echte betaalprovider.
 * <p>
 * De uitslag van de betaling komt later en los van de browser binnen. De adapter meldt die aan
 * {@link BetaalUitslagVerwerker}. Wat de browser na het betalen meestuurt naar de
 * {@link StartBetalingOpdracht#retourUrl() retour-URL}, is onbetrouwbaar en wordt niet gebruikt.
 */
public interface OnlineBetaalService {

    /**
     * Maakt de betaling aan bij de betaalprovider.
     *
     * @return de URL waar de browser van de burger heen moet om te betalen
     * @throws OnlineBetalenNietBeschikbaarException wanneer de betaalprovider de betaling niet kan aanmaken
     */
    BetalingGestart startBetaling(StartBetalingOpdracht opdracht);
}
