package nl.rotterdam.verbonden.core.online_betalen;

/**
 * Verwerkt de definitieve uitslag van een betaling. Geïmplementeerd door core; een adapter roept dit aan zodra
 * de betaalprovider de uitslag meldt.
 * <p>
 * Een bericht mag vaker komen: dezelfde uitslag nog eens verwerken verandert niets. Een onbekend kenmerk, of een
 * melding die niet bij de betaling past (ander kenmerk bij de betaalprovider, ander bedrag), wordt als fout gelogd
 * en verder genegeerd: opnieuw sturen helpt dan niet. Gooit deze methode een exceptie (bijvoorbeeld omdat de
 * database niet bereikbaar is), dan laat de adapter de betaalprovider het bericht later opnieuw sturen.
 */
public interface BetaalUitslagVerwerker {

    void verwerkUitslag(BetaalUitslagMelding melding);
}
