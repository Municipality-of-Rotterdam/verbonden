package nl.rotterdam.verbonden.core.persistence;

import nl.rotterdam.verbonden.core.domain.Bedrag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BedragAttributeConverterTest {

    private final BedragAttributeConverter converter = new BedragAttributeConverter();

    @Test
    void convertsBetweenBedragAndBigDecimal() {
        Bedrag bedrag = Bedrag.of("42.50");

        assertThat(converter.convertToDatabaseColumn(bedrag)).isEqualByComparingTo(new BigDecimal("42.50"));
        assertThat(converter.convertToEntityAttribute(new BigDecimal("42.50"))).isEqualTo(bedrag);
    }

    @Test
    void convertsNullValues() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
