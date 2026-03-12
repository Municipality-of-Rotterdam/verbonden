package nl.rotterdam.huwelijk.features.baps_administration.ui;

import nl.rotterdam.huwelijk.features.baps_administration.domain.PersonFullName;
import nl.rotterdam.huwelijk.features.baps_administration.domain.PersonFullNameTooLongException;
import nl.rotterdam.huwelijk.features.baps_administration.domain.PersonFullNameTooShortException;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.util.Locale;

public class PersonFullNameWicketConverter implements IConverter<PersonFullName> {

    @Override
    public PersonFullName convertToObject(String value, Locale locale) throws ConversionException {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new PersonFullName(value);
        } catch (PersonFullNameTooShortException e) {
            throw new ConversionException("Naam moet meer dan 4 tekens bevatten")
                    .setResourceKey("PersonFullName.tooShort");
        } catch (PersonFullNameTooLongException e) {
            throw new ConversionException("Naam moet minder dan 80 tekens bevatten")
                    .setResourceKey("PersonFullName.tooLong");
        }
    }

    @Override
    public String convertToString(PersonFullName value, Locale locale) {
        return value != null ? value.getValue() : "";
    }
}
