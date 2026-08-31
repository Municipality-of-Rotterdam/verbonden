package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.Telefoonnummer;

@Converter(autoApply = false)
public class TelefoonnummerAttributeConverter implements AttributeConverter<Telefoonnummer, String> {

    @Override
    public String convertToDatabaseColumn(Telefoonnummer attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public Telefoonnummer convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return new Telefoonnummer(dbData);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
