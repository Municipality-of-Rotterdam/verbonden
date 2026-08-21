package nl.rotterdam.verbonden.identity;

/**
 * Vertaalt de actieve {@code Authentication} (van welke bron dan ook) naar een
 * {@link AuthenticatedUser}. Aanspreekpunt voor applicatiecode in plaats van
 * directe {@code SecurityContextHolder}-aanroepen.
 */
public interface CurrentUserProvider {

    AuthenticatedUser getCurrentUser();
}
