package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.domain.Postcode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ChangeGetuigeDtoTest {

    @ParameterizedTest(name = "geboren {0}, ceremonie {1}: te jong = {2}")
    @CsvSource({
            "2008-06-15, 2026-06-14, true",
            "2008-06-15, 2026-06-15, false",
            "2008-06-15, 2026-06-16, false",
            // Jaarovergang
            "2008-01-01, 2025-12-31, true",
            "2008-01-01, 2026-01-01, false",
            // Geboren op 29 februari: in een gewoon jaar pas op 1 maart een jaar ouder
            "2008-02-29, 2026-02-28, true",
            "2008-02-29, 2026-03-01, false",
            "2008-02-29, 2028-02-29, false",
            "1970-05-01, 2026-06-15, false"
    })
    void isTeJongOp(LocalDate geboortedatum, LocalDate ceremonieDatum, boolean teJong) {
        assertThat(getuige(geboortedatum).isTeJongOp(ceremonieDatum)).isEqualTo(teJong);
    }

    @Test
    void isTeJongOp_zonderGeboortedatum_isNietTeJong() {
        assertThat(getuige(null).isTeJongOp(LocalDate.of(2026, 6, 15))).isFalse();
    }

    @Test
    void isLeeg_alleenAlsNietsIsIngevuld() {
        assertThat(new ChangeGetuigeDto(1, null, null, null, null, null, null, "Nederland").isLeeg()).isTrue();
        assertThat(new ChangeGetuigeDto(1, " ", "", null, null, null, null, null).isLeeg()).isTrue();
        assertThat(getuige(LocalDate.of(2000, 1, 1)).isLeeg()).isFalse();
        assertThat(new ChangeGetuigeDto(1, null, null, null, null, new Postcode("3011 AD"), null, null).isLeeg()).isFalse();
    }

    private static ChangeGetuigeDto getuige(LocalDate geboortedatum) {
        return new ChangeGetuigeDto(1, null, null, null, geboortedatum, null, null, null);
    }

    @ParameterizedTest(name = "postcode {0}, land {1}: ongeldig = {2}")
    @CsvSource({
            "3011 AD, Nederland, false",
            "3011 AD, ' nederland ', false",
            "B-1000, Nederland, true",
            "3011 SA, Nederland, true",
            "B-1000, België, false",
            "B-1000, , false",
            ", Nederland, false"
    })
    void heeftOngeldigeNederlandsePostcode(String postcode, String land, boolean ongeldig) {
        ChangeGetuigeDto getuige = new ChangeGetuigeDto(1, null, null, null, null,
                postcode != null ? new Postcode(postcode) : null, null, land);
        assertThat(getuige.heeftOngeldigeNederlandsePostcode()).isEqualTo(ongeldig);
    }
}
