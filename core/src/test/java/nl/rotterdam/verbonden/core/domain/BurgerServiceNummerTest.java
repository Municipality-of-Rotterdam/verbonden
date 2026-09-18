package nl.rotterdam.verbonden.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurgerServiceNummerTest {

    @Test
    void acceptsValidBsn() {
        BurgerServiceNummer bsn = new BurgerServiceNummer("123456782");
        assertThat(bsn.getValue()).isEqualTo("123456782");
    }

    @Test
    void padsWithLeadingZeros() {
        BurgerServiceNummer bsn = new BurgerServiceNummer("34567896");
        assertThat(bsn.getValue()).isEqualTo("034567896");
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new BurgerServiceNummer(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsBlank() {
        assertThatThrownBy(() -> new BurgerServiceNummer(""))
                .isInstanceOf(BurgerServiceNummerOngeldigException.class);
    }

    @Test
    void rejectsNonNumeric() {
        assertThatThrownBy(() -> new BurgerServiceNummer("12345678a"))
                .isInstanceOf(BurgerServiceNummerOngeldigException.class);
    }

    @Test
    void rejectsMoreThanNineDigits() {
        assertThatThrownBy(() -> new BurgerServiceNummer("1234567890"))
                .isInstanceOf(BurgerServiceNummerOngeldigException.class);
    }

    @Test
    void rejectsInvalidChecksum() {
        assertThatThrownBy(() -> new BurgerServiceNummer("123456789"))
                .isInstanceOf(BurgerServiceNummerOngeldigException.class);
    }

    @Test
    void toStringIncludesSimpleClassNameAndValue() {
        BurgerServiceNummer bsn = new BurgerServiceNummer("123456782");
        assertThat(bsn.toString()).isEqualTo("BurgerServiceNummer[value=123456782]");
    }

    @Test
    void equalityBasedOnValue() {
        assertThat(new BurgerServiceNummer("123456782")).isEqualTo(new BurgerServiceNummer("123456782"));
        assertThat(new BurgerServiceNummer("123456782")).isNotEqualTo(new BurgerServiceNummer("111222333"));
    }
}
