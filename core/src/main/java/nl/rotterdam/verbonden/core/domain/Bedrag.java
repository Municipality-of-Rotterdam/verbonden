package nl.rotterdam.verbonden.core.domain;

import jakarta.annotation.Nonnull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

public record Bedrag(BigDecimal value) implements ValueHolder<BigDecimal>, Comparable<Bedrag>, Serializable {

    public static final Bedrag ZERO = new Bedrag(BigDecimal.ZERO);
    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.forLanguageTag("nl-NL"));
    private static final DecimalFormat WHOLE_FORMAT = new DecimalFormat("#,##0", SYMBOLS);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00", SYMBOLS);


    public Bedrag {
        Objects.requireNonNull(value, "Bedrag mag niet null zijn");
        if (value.stripTrailingZeros().scale() > 2) {
            throw new BedragTeVeelDecimalenException(value);
        }
        value = value.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static Bedrag of(String value) {
        return new Bedrag(new BigDecimal(value));
    }

    public Bedrag plus(Bedrag bedrag) {
        return new Bedrag(value.add(Objects.requireNonNull(bedrag).value));
    }

    public Bedrag minus(Bedrag bedrag) {
        return new Bedrag(value.subtract(Objects.requireNonNull(bedrag).value));
    }

    public boolean isPositive() {
        return value.signum() > 0;
    }

    public String format() {
        // Check if the number is effectively a whole integer
        if (value.stripTrailingZeros().scale() <= 0) {
            return WHOLE_FORMAT.format(value);
        }
        return DECIMAL_FORMAT.format(value);
    }

    @Override
    public BigDecimal getValue() {
        return value;
    }

    @Override
    public int compareTo(Bedrag other) {
        return value.compareTo(other.value);
    }

    @Override
    @Nonnull public String toString() {
        return "Bedrag[" + value.toPlainString() + "]";
    }
}
