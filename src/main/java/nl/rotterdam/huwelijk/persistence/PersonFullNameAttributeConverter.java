package nl.rotterdam.huwelijk.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import nl.rotterdam.huwelijk.features.baps_administration.domain.PersonFullName;

@Converter(autoApply = false)
public class PersonFullNameAttributeConverter implements AttributeConverter<PersonFullName, String> {

    @Override
    public String convertToDatabaseColumn(PersonFullName attribute) {
        return attribute != null ? attribute.value() : null;
    }

    @Override
    public PersonFullName convertToEntityAttribute(String dbData) {
        return dbData != null ? new PersonFullName(dbData) : null;
    }
}
