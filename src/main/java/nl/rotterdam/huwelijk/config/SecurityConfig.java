package nl.rotterdam.huwelijk.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security configuration.
 *
 * <p>Two filter chains are defined:
 * <ol>
 *   <li><b>Admin chain</b> ({@code /beheer/**}): protects the administration section.
 *       Administrators are configured via the {@code beheer.gebruikers} property.</li>
 *   <li><b>Burger chain</b> (everything else): protects citizen-facing pages.
 *       Uses a mock DigiD login page for development/test.
 *       For production, replace the {@code exceptionHandling} entry point with
 *       {@code .oauth2Login(...)} to integrate a real OIDC/DigiD provider.</li>
 * </ol>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * Comma-separated list of {@code gebruikersnaam:wachtwoord} pairs for administrators.
     * Falls back to a default development account when not configured.
     */
    @Value("${beheer.gebruikers:}")
    private String gebruikersConfig;

    /**
     * Security filter chain for the /beheer administration section.
     * Uses Spring Security's built-in form login. The login page is served at /login,
     * which is also included in this chain's matcher so it is handled correctly.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(new OrRequestMatcher(
                        new AntPathRequestMatcher("/beheer/**"),
                        new AntPathRequestMatcher("/login"),
                        new AntPathRequestMatcher("/login/**"),
                        new AntPathRequestMatcher("/logout")
                ))
                // Apache Wicket 10 has its own CSRF protection; disable Spring Security's to avoid conflicts.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/login/**").permitAll()
                        .requestMatchers("/beheer/**").hasRole("BEHEERDER")
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .defaultSuccessUrl("/beheer", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );
        return http.build();
    }

    /**
     * Security filter chain for citizen-facing pages.
     *
     * <p>Requires authentication for all pages except the mock DigiD login page at
     * {@code /inloggen} and static Wicket resources. Unauthenticated visitors are
     * redirected to {@code /inloggen}.
     *
     * <p><b>Replacing mock DigiD with real OIDC for production:</b>
     * Remove the {@code exceptionHandling} configuration and add
     * {@code .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/", true))}
     * along with the appropriate OIDC provider properties in {@code application.properties}.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain burgerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/inloggen", "/inloggen/**").permitAll()
                        .requestMatchers("/wicket/resource/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/inloggen"))
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/uitloggen", "GET"))
                        .logoutSuccessUrl("/inloggen")
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        List<UserDetails> users = new ArrayList<>();

        if (gebruikersConfig != null && !gebruikersConfig.isBlank()) {
            for (String entry : gebruikersConfig.split(",")) {
                String[] parts = entry.trim().split(":", 2);
                if (parts.length == 2 && !parts[0].isBlank() && !parts[1].isBlank()) {
                    // Passwords may carry an encoding prefix ({noop}, {bcrypt}, …).
                    // Plain passwords without a prefix are treated as {noop} (clear text),
                    // which is acceptable for local development.  Use {bcrypt} hashes in
                    // production; e.g. beheer.gebruikers=alice:{bcrypt}$2a$10$…
                    String rawPassword = parts[1].trim();
                    String encodedPassword = rawPassword.startsWith("{")
                            ? rawPassword
                            : "{noop}" + rawPassword;
                    users.add(User.builder()
                            .username(parts[0].trim())
                            .password(encodedPassword)
                            .roles("BEHEERDER")
                            .build());
                }
            }
        }

        if (users.isEmpty()) {
            // Default fallback account for development – always override via beheer.gebruikers.
            log.warn("No beheer.gebruikers configured – falling back to default development "
                    + "account 'beheerder'. Do NOT use this in production.");
            users.add(User.builder()
                    .username("beheerder")
                    .password("{noop}rotterdam")
                    .roles("BEHEERDER")
                    .build());
        }

        return new InMemoryUserDetailsManager(users);
    }
}
