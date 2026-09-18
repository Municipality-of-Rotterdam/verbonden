package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;

@Converter
public class BurgerServiceNummerAttributeConverter implements AttributeConverter<BurgerServiceNummer, String> {

    @Override
    public String convertToDatabaseColumn(BurgerServiceNummer attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public BurgerServiceNummer convertToEntityAttribute(String dbData) {
        return dbData != null ? new BurgerServiceNummer(dbData) : null;
    }
}
