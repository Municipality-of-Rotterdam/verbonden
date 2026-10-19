package nl.rotterdam.verbonden.core.integration_test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Een klok die een test op een vaste datum kan zetten, in plaats van de systeemklok in {@link
 * nl.rotterdam.verbonden.core.config.ClockConfig}. Zonder {@link #zetOp} loopt hij gelijk met de systeemklok;
 * zet hem na de test terug met {@link #zetTerug}, want de Spring-context wordt gedeeld tussen testklassen.
 */
public class InstelbareKlok extends Clock {

    private Clock klok = Clock.systemDefaultZone();

    /** Zet de klok vast op het midden van {@code datum}, zodat geen test rond middernacht anders uitvalt. */
    public void zetOp(LocalDate datum) {
        ZoneId zone = ZoneId.systemDefault();
        klok = Clock.fixed(datum.atTime(LocalTime.NOON).atZone(zone).toInstant(), zone);
    }

    public void zetTerug() {
        klok = Clock.systemDefaultZone();
    }

    @Override
    public ZoneId getZone() {
        return klok.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return klok.withZone(zone);
    }

    @Override
    public Instant instant() {
        return klok.instant();
    }

    @TestConfiguration
    static class Config {

        @Bean
        @Primary
        InstelbareKlok instelbareKlok() {
            return new InstelbareKlok();
        }
    }
}
