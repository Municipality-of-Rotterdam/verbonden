package nl.rotterdam.verbonden.core.ui;

import nl.rotterdam.verbonden.core.domain.Bedrag;
import org.apache.wicket.util.convert.ConversionException;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BedragWicketConverterTest {

    private final BedragWicketConverter converter = new BedragWicketConverter();

    @Test
    void convertToObject_accepteertKommaAlsDecimaalteken() {
        assertThat(converter.convertToObject("42,50", Locale.forLanguageTag("nl-NL")))
                .isEqualTo(Bedrag.of("42.50"));
    }

    @Test
    void convertToObject_geeftNullVoorLegeInvoer() {
        assertThat(converter.convertToObject("  ", Locale.forLanguageTag("nl-NL"))).isNull();
    }

    @Test
    void convertToObject_weigertOngeldigeInvoer() {
        assertThatThrownBy(() -> converter.convertToObject("ongeldig", Locale.forLanguageTag("nl-NL")))
                .isInstanceOf(ConversionException.class);
    }
}
