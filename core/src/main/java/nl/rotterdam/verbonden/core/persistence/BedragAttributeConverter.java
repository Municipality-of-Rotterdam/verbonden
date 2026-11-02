package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.math.BigDecimal;

@Converter
public class BedragAttributeConverter implements AttributeConverter<Bedrag, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Bedrag attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public Bedrag convertToEntityAttribute(BigDecimal dbData) {
        return dbData != null ? new Bedrag(dbData) : null;
    }
}
