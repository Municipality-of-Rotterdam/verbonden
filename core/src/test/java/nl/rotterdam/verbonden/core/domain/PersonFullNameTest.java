package nl.rotterdam.verbonden.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonFullNameTest {

    @Test
    void constructor_staatLettersEnNaamtekensToe() {
        assertThat(new PersonFullName("Élodie O'Neil-Smith Jr.").getValue())
                .isEqualTo("Élodie O'Neil-Smith Jr.");
    }

    @Test
    void constructor_staatTypografischeApostrofToe() {
        assertThat(new PersonFullName("John O’Connor").getValue())
                .isEqualTo("John O’Connor");
    }

    @Test
    void constructor_staatKorteChineseNaamToe() {
        assertThat(new PersonFullName("Li").getValue()).isEqualTo("Li");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "محمد عبد الله",   // Arabisch
            "مُحَمَّد",          // Arabisch met klinkertekens
            "李明",             // Chinees
            "王小红",           // Chinees
            "山田 太郎",        // Japans, kanji
            "やまだ はなこ",    // Japans, hiragana
            "スズキ イチロー",  // Japans, katakana
    })
    void constructor_staatNamenInAndereSchriftenToe(String naam) {
        assertThat(new PersonFullName(naam).getValue()).isEqualTo(naam);
    }

    @Test
    void constructor_weigertOngeldigeTekens() {
        assertThatThrownBy(() -> new PersonFullName("<script>"))
                .isInstanceOf(PersonFullNameInvalidException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"--", "...", "' '", "- . ’"})
    void constructor_weigertNaamZonderLetter(String naam) {
        assertThatThrownBy(() -> new PersonFullName(naam))
                .isInstanceOf(PersonFullNameInvalidException.class);
    }

    @Test
    void constructor_blijftTeKorteNamenWeigeren() {
        assertThatThrownBy(() -> new PersonFullName("J"))
                .isInstanceOf(PersonFullNameTooShortException.class);
    }
}
