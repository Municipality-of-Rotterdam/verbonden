package nl.rotterdam.verbonden.core.domain;

import java.math.BigDecimal;

public class BedragTeVeelDecimalenException extends RuntimeException {

    public BedragTeVeelDecimalenException(BigDecimal value) {
        super("Bedrag heeft meer dan twee decimalen: '" + value.toPlainString() + "'");
    }
}
