package nl.rotterdam.verbonden.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostcodeTest {

    @ParameterizedTest(name = "''{0}'' wordt ''{1}''")
    @CsvSource(delimiter = '|', value = {
            // Nederland: spatie mag ontbreken, letters worden hoofdletters
            "3011 AD|3011 AD",
            "3011AD|3011 AD",
            "3011ad|3011 AD",
            " 3011  aD |3011 AD",
            // Andere landen
            "1000|1000",
            "b-1000|B-1000",
            "10115|10115",
            "sw1a  1aa|SW1A 1AA",
            "K1A 0B1|K1A 0B1",
            "12345-6789|12345-6789",
            "101|101"
    })
    void normaliseert(String invoer, String verwacht) {
        assertThat(new Postcode(invoer).getValue()).isEqualTo(verwacht);
    }

    @ParameterizedTest(name = "''{0}'' is ongeldig")
    @ValueSource(strings = {"", "   ", "-1000", "3011 AD!", "12345-67890", "3011_AD", "3011 Á1"})
    void weigertOngeldigePostcode(String invoer) {
        assertThatThrownBy(() -> new Postcode(invoer)).isInstanceOf(PostcodeOngeldigException.class);
    }

    @Test
    void weigertNull() {
        assertThatThrownBy(() -> new Postcode(null)).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest(name = "''{0}'' is Nederlands: {1}")
    @CsvSource({
            "3011ad, true",
            "9999 ZZ, true",
            "0123 AB, false",
            "3011 SA, false",
            "3011 sd, false",
            "3011 SS, false",
            "B-1000, false",
            "10115, false"
    })
    void isNederlands(String invoer, boolean nederlands) {
        assertThat(new Postcode(invoer).isNederlands()).isEqualTo(nederlands);
    }
}
