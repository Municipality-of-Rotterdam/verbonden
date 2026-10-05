package nl.rotterdam.verbonden.core.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Extension point for a production adapter module to customize
 * {@link SecurityConfig#burgerSecurityFilterChain(HttpSecurity)}, e.g. to register a
 * pre-authentication filter fed by an upstream gateway header instead of (or alongside) the
 * {@code oauth2Login} path. Implement this as a bean in the adapter module; when no such
 * bean is present, the burger chain behaves exactly as before.
 *
 * <p>Deliberately not a {@code Customizer<HttpSecurity>}: Spring Security 7 applies every bean of
 * that type to <em>every</em> {@code HttpSecurity}, i.e. to both filter chains, on top of the
 * explicit call in {@link SecurityConfig}.
 */
@FunctionalInterface
public interface BurgerSecurityCustomizer {

    void customize(HttpSecurity http);
}
