package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.Emailadres;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.EmailadresOngeldigException;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public class EmailadresWicketConverter implements IConverter<Emailadres> {

    @Override
    public @Nullable Emailadres convertToObject(String value, Locale locale) throws ConversionException {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new Emailadres(value);
        } catch (EmailadresOngeldigException e) {
            throw new ConversionException("Emailadres is ongeldig")
                    .setResourceKey("Emailadres.ongeldig")
                    .setLocale(locale);
        }
    }

    @Override
    public String convertToString(Emailadres value, Locale locale) {
        return value.getValue();
    }
}
