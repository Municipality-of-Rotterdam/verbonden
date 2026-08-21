package nl.rotterdam.verbonden.features.marriage_intake.ui;

import nl.rotterdam.verbonden.features.marriage_intake.domain.Emailadres;
import nl.rotterdam.verbonden.features.marriage_intake.domain.EmailadresOngeldigException;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.util.Locale;

public class EmailadresWicketConverter implements IConverter<Emailadres> {

    @Override
    public Emailadres convertToObject(String value, Locale locale) throws ConversionException {
        try {
            return new Emailadres(value);
        } catch (EmailadresOngeldigException e) {
            throw new ConversionException("Emailadres is ongeldig")
                    .setResourceKey("EmailadresValidator")
                    .setLocale(locale);
        }
    }

    @Override
    public String convertToString(Emailadres value, Locale locale) {
        return value.getValue();
    }
}
