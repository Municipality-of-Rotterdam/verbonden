package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.rotterdam.verbonden.core.domain.BuitenlandsPersoonsnummer;

@Converter
public class BuitenlandsPersoonsnummerAttributeConverter implements AttributeConverter<BuitenlandsPersoonsnummer, String> {

    @Override
    public String convertToDatabaseColumn(BuitenlandsPersoonsnummer attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public BuitenlandsPersoonsnummer convertToEntityAttribute(String dbData) {
        return dbData != null ? new BuitenlandsPersoonsnummer(dbData) : null;
    }
}
