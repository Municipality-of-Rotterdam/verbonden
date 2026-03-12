package nl.rotterdam.huwelijk.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security configuration for the /beheer administration section.
 *
 * <p>Administrators are configured via the {@code beheer.gebruikers} property using the format:
 * {@code gebruiker1:wachtwoord1,gebruiker2:wachtwoord2}
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * Comma-separated list of {@code gebruikersnaam:wachtwoord} pairs.
     * Falls back to a default development account when not configured.
     */
    @Value("${beheer.gebruikers:}")
    private String gebruikersConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Apache Wicket 10 enables its own CSRF protection (CsrfPreventionRequestCycleListener)
                // by default, so Spring Security's CSRF filter is disabled here to avoid conflicts.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/beheer/**").authenticated()
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
