package nl.rotterdam.verbonden.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BedragTest {

    @Test
    void format_gebruiktNederlandseNotatie() {
        assertThat(Bedrag.of("1234.5").format()).isEqualTo("1.234,50");
    }

    @Test
    void plus_teltBedragenOp() {
        assertThat(Bedrag.of("17.80").plus(Bedrag.of("42.50"))).isEqualTo(Bedrag.of("60.30"));
    }

    @Test
    void constructor_weigertAfrondenVanExtraDecimalen() {
        assertThatThrownBy(() -> Bedrag.of("1.001"))
                .isInstanceOf(ArithmeticException.class);
    }
}
