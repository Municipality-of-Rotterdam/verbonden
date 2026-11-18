package nl.rotterdam.verbonden.core.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Nieuwe Flyway-migraties heten {@code VYYYY.MM.DD.HH.MM__omschrijving.sql}, zodat branches die tegelijk worden
 * ontwikkeld niet hetzelfde versienummer kiezen (zie {@code FlywayConfig}). Alleen de migraties van vóór deze
 * afspraak (V1 tot en met V44) hebben nog een volgnummer.
 */
class MigratieNamenTest {

    private static final int LAATSTE_VOLGNUMMER = 44;
    private static final Pattern VOLGNUMMER = Pattern.compile("V(\\d+)__\\w+\\.sql");
    private static final Pattern TIJDSTIP = Pattern.compile("V\\d{4}\\.\\d{2}\\.\\d{2}\\.\\d{2}\\.\\d{2}__\\w+\\.sql");

    @Test
    void nieuweMigratiesHebbenEenTijdstipAlsVersie() throws IOException {
        Resource[] migraties = new PathMatchingResourcePatternResolver()
                .getResources("classpath:db/migration/V*.sql");

        assertThat(migraties).isNotEmpty();
        assertThat(Arrays.stream(migraties).map(Resource::getFilename))
                .allSatisfy(naam -> assertThat(isGeldig(naam))
                        .as("%s heet niet VYYYY.MM.DD.HH.MM__omschrijving.sql", naam)
                        .isTrue());
    }

    private static boolean isGeldig(String naam) {
        var volgnummer = VOLGNUMMER.matcher(naam);
        if (volgnummer.matches()) {
            return Integer.parseInt(volgnummer.group(1)) <= LAATSTE_VOLGNUMMER;
        }
        return TIJDSTIP.matcher(naam).matches();
    }
}
