package nl.rotterdam.huwelijk.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.Telefoonnummer;

@Converter(autoApply = false)
public class TelefoonnummerAttributeConverter implements AttributeConverter<Telefoonnummer, String> {

    @Override
    public String convertToDatabaseColumn(Telefoonnummer attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public Telefoonnummer convertToEntityAttribute(String dbData) {
        return dbData != null ? new Telefoonnummer(dbData) : null;
    }
}
