package nl.rotterdam.verbonden.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonFullNameTest {

    @Test
    void constructor_staatLettersEnNaamtekensToe() {
        assertThat(new PersonFullName("Élodie O'Neil-Smith Jr.").getValue())
                .isEqualTo("Élodie O'Neil-Smith Jr.");
    }

    @Test
    void constructor_weigertOngeldigeTekens() {
        assertThatThrownBy(() -> new PersonFullName("<script>"))
                .isInstanceOf(PersonFullNameInvalidException.class);
    }

    @Test
    void constructor_blijftTeKorteNamenWeigeren() {
        assertThatThrownBy(() -> new PersonFullName("Jan"))
                .isInstanceOf(PersonFullNameTooShortException.class);
    }
}
