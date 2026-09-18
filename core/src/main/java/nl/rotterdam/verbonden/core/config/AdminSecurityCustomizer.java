package nl.rotterdam.verbonden.core.config;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Extension point for a production adapter module to customize
 * {@link SecurityConfig#adminSecurityFilterChain(HttpSecurity)}, e.g. to grant
 * {@code ROLE_BEHEERDER} to internal users identified through an upstream gateway header
 * (alongside, or instead of, the built-in {@code formLogin}). Implement this as a bean in the
 * adapter module; when no such bean is present, the admin chain behaves exactly as before.
 */
public interface AdminSecurityCustomizer extends Customizer<HttpSecurity> {
}
