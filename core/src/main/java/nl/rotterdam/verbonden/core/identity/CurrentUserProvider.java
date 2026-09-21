package nl.rotterdam.verbonden.core.identity;

/**
 * Vertaalt de actieve {@code Authentication} (van welke bron dan ook) naar een
 * {@link AuthenticatedUser}. Aanspreekpunt voor applicatiecode in plaats van
 * directe {@code SecurityContextHolder}-aanroepen.
 *
 * <p>{@link AuthenticatedUser#getUserId()} is voor een burger het kale BSN. De
 * standaardimplementatie leunt op {@code Authentication.getName()}, wat bij een principal die
 * geen {@code String}, {@code Principal} of {@code UserDetails} is terugvalt op
 * {@code toString()}. Een adapter die zo'n principal in de {@code SecurityContext} zet, moet dus
 * een eigen implementatie (met {@code @Primary}) aanbieden die de principal uitpakt.
 */
public interface CurrentUserProvider {

    AuthenticatedUser getCurrentUser();
}
