package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

final class IntakePriceFormatter {

    private IntakePriceFormatter() {
    }

    static String format(BigDecimal price) {
        return new DecimalFormat("#,##0.00",
                new DecimalFormatSymbols(Locale.forLanguageTag("nl-NL")))
                .format(Objects.requireNonNull(price));
    }
}
