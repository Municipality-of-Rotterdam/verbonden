package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class InternationaleAkteTariefTest {

    @Test
    void aanvraagIn2026_kost1780() {
        assertThat(InternationaleAkteTarief.prijsOp(LocalDate.of(2026, 1, 1))).isEqualByComparingTo(new BigDecimal("17.80"));
        assertThat(InternationaleAkteTarief.prijsOp(LocalDate.of(2026, 12, 31))).isEqualByComparingTo(new BigDecimal("17.80"));
    }

    @Test
    void aanvraagVanaf1Januari2027_kost1830() {
        assertThat(InternationaleAkteTarief.prijsOp(LocalDate.of(2027, 1, 1))).isEqualByComparingTo(new BigDecimal("18.30"));
        assertThat(InternationaleAkteTarief.prijsOp(LocalDate.of(2028, 6, 15))).isEqualByComparingTo(new BigDecimal("18.30"));
    }
}
