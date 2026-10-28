package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class IntakePriceFormatterTest {

    @Test
    void formatsPriceUsingDutchSeparators() {
        assertThat(IntakePriceFormatter.format(new BigDecimal("1234.5"))).isEqualTo("1.234,50");
    }
}
