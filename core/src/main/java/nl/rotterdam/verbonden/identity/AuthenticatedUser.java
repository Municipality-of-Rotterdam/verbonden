package nl.rotterdam.verbonden.identity;

import java.util.Set;

/**
 * Eén representatie voor een ingelogde gebruiker, ongeacht of de authenticatie
 * tot stand kwam via een burger-inlogroute (mock-DigiD of echte DigiD-OIDC)
 * of via de medewerker-inlogroute (admin form-login).
 */
public interface AuthenticatedUser {

    String getUserId();

    Set<String> getRoles();

    default boolean hasRole(String role) {
        return getRoles().contains(role);
    }
}
