package nl.rotterdam.verbonden.core.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

public record Bedrag(BigDecimal value) implements ValueHolder<BigDecimal>, Comparable<Bedrag>, Serializable {

    public static final Bedrag ZERO = new Bedrag(BigDecimal.ZERO);

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

    public boolean isPositive() {
        return value.signum() > 0;
    }

    public String format() {
        return new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.forLanguageTag("nl-NL")))
                .format(value);
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
    public String toString() {
        return "Bedrag[" + value.toPlainString() + "]";
    }
}
