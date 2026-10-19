package nl.rotterdam.verbonden.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * De klok waarmee services de huidige datum en tijd bepalen. Tests vervangen hem door een vaste klok, zodat
 * datum-afhankelijke logica reproduceerbaar te testen is.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
