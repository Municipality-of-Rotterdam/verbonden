package nl.rotterdam.verbonden.core.config;

import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway voert ook migraties uit die een lagere versie hebben dan de laatst uitgevoerde migratie ({@code outOfOrder}).
 * <p>
 * Nieuwe migraties krijgen als versie het moment van aanmaken, {@code VYYYY.MM.DD.HH.MM__omschrijving.sql}
 * (bijvoorbeeld {@code V2026.11.02.14.40__create_dossier_mails.sql}), zodat branches die tegelijk worden ontwikkeld
 * niet hetzelfde versienummer kiezen. Wordt een branch later gemerged dan een branch met een nieuwere migratie, dan
 * is zijn migratie op een bestaande database "te oud"; dankzij {@code outOfOrder} wordt die toch uitgevoerd.
 */
@Configuration
public class FlywayConfig {

    @Bean
    FlywayConfigurationCustomizer migratiesBuitenVolgorde() {
        return configuration -> configuration.outOfOrder(true);
    }
}
