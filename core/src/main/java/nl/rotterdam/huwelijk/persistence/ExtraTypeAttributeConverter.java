package nl.rotterdam.huwelijk.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.rotterdam.huwelijk.features.extra_administration.domain.ExtraType;

@Converter(autoApply = false)
public class ExtraTypeAttributeConverter implements AttributeConverter<ExtraType, String> {

    @Override
    public String convertToDatabaseColumn(ExtraType attribute) {
        return attribute != null ? attribute.getDbValue() : null;
    }

    @Override
    public ExtraType convertToEntityAttribute(String dbData) {
        return dbData != null ? ExtraType.fromDbValue(dbData) : null;
    }
}
