package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.rotterdam.verbonden.core.domain.Postcode;

@Converter
public class PostcodeAttributeConverter implements AttributeConverter<Postcode, String> {

    @Override
    public String convertToDatabaseColumn(Postcode attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public Postcode convertToEntityAttribute(String dbData) {
        return dbData != null ? new Postcode(dbData) : null;
    }
}
