package nl.rotterdam.verbonden.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BuitenlandsPersoonsnummerTest {

    @Test
    void accepteertSocialSecurityNumberEnVerwijdertSpatiesRondom() {
        assertThat(new BuitenlandsPersoonsnummer("  123-45-6789 ").getValue()).isEqualTo("123-45-6789");
    }

    @Test
    void accepteertLettersEnScheidingstekens() {
        assertThat(new BuitenlandsPersoonsnummer("AB 12.34/56").getValue()).isEqualTo("AB 12.34/56");
    }

    @Test
    void weigertLeegNummer() {
        assertThatThrownBy(() -> new BuitenlandsPersoonsnummer("   "))
                .isInstanceOf(BuitenlandsPersoonsnummerOngeldigException.class);
    }

    @Test
    void weigertTeLangNummer() {
        assertThatThrownBy(() -> new BuitenlandsPersoonsnummer("1".repeat(51)))
                .isInstanceOf(BuitenlandsPersoonsnummerOngeldigException.class);
    }

    @Test
    void weigertAndereTekens() {
        assertThatThrownBy(() -> new BuitenlandsPersoonsnummer("123<script>"))
                .isInstanceOf(BuitenlandsPersoonsnummerOngeldigException.class);
    }
}
