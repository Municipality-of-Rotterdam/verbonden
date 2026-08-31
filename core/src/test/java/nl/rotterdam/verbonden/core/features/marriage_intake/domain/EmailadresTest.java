package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailadresTest {

    @Test
    void acceptsSimpleEmailAddress() {
        Emailadres email = new Emailadres("gebruiker@example.com");
        assertThat(email.getValue()).isEqualTo("gebruiker@example.com");
    }

    @Test
    void acceptsEmailWithSubdomain() {
        Emailadres email = new Emailadres("test@mail.example.com");
        assertThat(email.getValue()).isEqualTo("test@mail.example.com");
    }

    @Test
    void acceptsEmailWithPlusSigns() {
        Emailadres email = new Emailadres("test+label@example.com");
        assertThat(email.getValue()).isEqualTo("test+label@example.com");
    }

    @Test
    void trimsLeadingAndTrailingWhitespace() {
        Emailadres email = new Emailadres("  test@example.com  ");
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    void rejectsAddressWithoutAtSign() {
        assertThatThrownBy(() -> new Emailadres("noatsign.com"))
                .isInstanceOf(EmailadresOngeldigException.class);
    }

    @Test
    void rejectsAddressWithoutDomain() {
        assertThatThrownBy(() -> new Emailadres("test@"))
                .isInstanceOf(EmailadresOngeldigException.class);
    }

    @Test
    void rejectsAddressWithoutTld() {
        assertThatThrownBy(() -> new Emailadres("test@nodot"))
                .isInstanceOf(EmailadresOngeldigException.class);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new Emailadres(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toStringIncludesSimpleClassNameAndValue() {
        Emailadres email = new Emailadres("test@example.com");
        assertThat(email.toString()).isEqualTo("Emailadres[test@example.com]");
    }

    @Test
    void getValueReturnsRawValue() {
        Emailadres email = new Emailadres("test@example.com");
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    void equalityBasedOnValue() {
        assertThat(new Emailadres("a@example.com")).isEqualTo(new Emailadres("a@example.com"));
        assertThat(new Emailadres("a@example.com")).isNotEqualTo(new Emailadres("b@example.com"));
    }
}
