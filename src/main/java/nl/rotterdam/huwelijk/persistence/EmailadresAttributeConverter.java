package nl.rotterdam.huwelijk.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.Emailadres;

@Converter(autoApply = false)
public class EmailadresAttributeConverter implements AttributeConverter<Emailadres, String> {

    @Override
    public String convertToDatabaseColumn(Emailadres attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public Emailadres convertToEntityAttribute(String dbData) {
        return dbData != null ? new Emailadres(dbData) : null;
    }
}
