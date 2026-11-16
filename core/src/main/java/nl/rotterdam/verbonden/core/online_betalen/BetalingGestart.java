package nl.rotterdam.verbonden.core.online_betalen;

/**
 * @param redirectUrl       de URL waar de browser van de burger heen moet om te betalen
 * @param externeReferentie het kenmerk van de betaling bij de betaalprovider, om de betaling daar terug te vinden
 */
public record BetalingGestart(String redirectUrl, String externeReferentie) {
}
