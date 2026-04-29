package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelefoonnummerTest {

    @Test
    void acceptsMobileNumberWithDash() {
        Telefoonnummer nummer = new Telefoonnummer("06-12345678");
        assertThat(nummer.getValue()).isEqualTo("06-12345678");
    }

    @Test
    void acceptsMobileNumberWithoutDash() {
        Telefoonnummer nummer = new Telefoonnummer("0612345678");
        assertThat(nummer.getValue()).isEqualTo("0612345678");
    }

    @Test
    void acceptsInternationalNlFormat() {
        Telefoonnummer nummer = new Telefoonnummer("+31612345678");
        assertThat(nummer.getValue()).isEqualTo("+31612345678");
    }

    @Test
    void acceptsLandlineNumber() {
        Telefoonnummer nummer = new Telefoonnummer("0201234567");
        assertThat(nummer.getValue()).isEqualTo("0201234567");
    }

    @Test
    void acceptsInternationalNonNlNumber() {
        Telefoonnummer nummer = new Telefoonnummer("+32 470 12 34 56");
        assertThat(nummer.getValue()).isEqualTo("+32 470 12 34 56");
    }

    @Test
    void trimsLeadingAndTrailingWhitespace() {
        Telefoonnummer nummer = new Telefoonnummer("  0612345678  ");
        assertThat(nummer.getValue()).isEqualTo("0612345678");
    }

    @Test
    void rejectsInvalidFormat() {
        assertThatThrownBy(() -> new Telefoonnummer("invalid"))
                .isInstanceOf(TelefoonnummerOngeldigException.class);
    }

    @Test
    void rejectsInvalidNumber() {
        assertThatThrownBy(() -> new Telefoonnummer("0012345678"))
                .isInstanceOf(TelefoonnummerOngeldigException.class);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new Telefoonnummer(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toStringIncludesSimpleClassNameAndValue() {
        Telefoonnummer nummer = new Telefoonnummer("0612345678");
        assertThat(nummer.toString()).isEqualTo("Telefoonnummer[0612345678]");
    }

    @Test
    void getValueReturnsRawValue() {
        Telefoonnummer nummer = new Telefoonnummer("06-12345678");
        assertThat(nummer.getValue()).isEqualTo("06-12345678");
    }

    @Test
    void equalityBasedOnValue() {
        assertThat(new Telefoonnummer("0612345678")).isEqualTo(new Telefoonnummer("0612345678"));
        assertThat(new Telefoonnummer("0612345678")).isNotEqualTo(new Telefoonnummer("0623456789"));
    }
}
