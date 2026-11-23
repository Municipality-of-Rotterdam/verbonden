package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.domain.Bedrag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static nl.rotterdam.verbonden.core.domain.Bedrag.of;
import static org.assertj.core.api.Assertions.assertThat;

class VrijeLocatieTariefTest {

    @Test
    void normaleDagGebruiktHetTariefVanHetGekozenTijdvak() {
        assertThat(VrijeLocatieTarief.bepaal(LocalDate.of(2026, 4, 20), of("1084.30"),
                List.of(of("2168.60"), of("770.90"), of("1084.30"))))
                .isEqualTo(of("1084.30"));
    }

    @Test
    void officiëleFeestdagGebruiktHetHoogsteBeheerdeDagTarief() {
        List<Bedrag> dagTarieven =
                List.of(of("2168.60"), of("770.90"), of("1084.30"));
        assertThat(VrijeLocatieTarief.bepaal(LocalDate.of(2026, 1, 1), of("770.90"), dagTarieven))
                .isEqualTo(of("2168.60"));
        assertThat(VrijeLocatieTarief.bepaal(LocalDate.of(2026, 4, 3), of("770.90"), dagTarieven))
                .isEqualTo(of("2168.60"));
        assertThat(VrijeLocatieTarief.bepaal(LocalDate.of(2026, 4, 27), of("770.90"), dagTarieven))
                .isEqualTo(of("2168.60"));
        assertThat(VrijeLocatieTarief.bepaal(LocalDate.of(2026, 4, 5), of("770.90"), dagTarieven))
                .isEqualTo(of("2168.60"));
        assertThat(VrijeLocatieTarief.bepaal(LocalDate.of(2026, 4, 6), of("770.90"), dagTarieven))
                .isEqualTo(of("2168.60"));
        assertThat(VrijeLocatieTarief.bepaal(LocalDate.of(2026, 5, 5), of("770.90"), dagTarieven))
                .isEqualTo(of("2168.60"));
        assertThat(VrijeLocatieTarief.bepaal(LocalDate.of(2026, 5, 14), of("770.90"), dagTarieven))
                .isEqualTo(of("2168.60"));
        assertThat(VrijeLocatieTarief.bepaal(LocalDate.of(2026, 5, 24), of("770.90"), dagTarieven))
                .isEqualTo(of("2168.60"));
        assertThat(VrijeLocatieTarief.bepaal(LocalDate.of(2026, 5, 25), of("770.90"), dagTarieven))
                .isEqualTo(of("2168.60"));
        assertThat(VrijeLocatieTarief.bepaal(LocalDate.of(2026, 12, 25), of("770.90"), dagTarieven))
                .isEqualTo(of("2168.60"));
        assertThat(NederlandseFeestdagen.isFeestdag(LocalDate.of(2025, 4, 26))).isTrue();
    }

    @Test
    void zondagGebruiktHetHoogsteBeheerdeDagTarief() {
        assertThat(VrijeLocatieTarief.bepaal(LocalDate.of(2026, 4, 19), of("770.90"),
                List.of(of("2168.60"), of("770.90"))))
                .isEqualTo(of("2168.60"));
    }
}
