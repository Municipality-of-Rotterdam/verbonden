package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.Emailadres;

@Converter(autoApply = false)
public class EmailadresAttributeConverter implements AttributeConverter<Emailadres, String> {

    @Override
    public String convertToDatabaseColumn(Emailadres attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public Emailadres convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return new Emailadres(dbData);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
