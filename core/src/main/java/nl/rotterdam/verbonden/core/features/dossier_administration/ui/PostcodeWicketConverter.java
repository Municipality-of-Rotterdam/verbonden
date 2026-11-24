package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.domain.Postcode;
import nl.rotterdam.verbonden.core.domain.PostcodeOngeldigException;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.util.Locale;

public class PostcodeWicketConverter implements IConverter<Postcode> {

    @Override
    public Postcode convertToObject(String value, Locale locale) throws ConversionException {
        try {
            return new Postcode(value);
        } catch (PostcodeOngeldigException e) {
            throw new ConversionException("Postcode is ongeldig")
                    .setResourceKey("PostcodeValidator")
                    .setLocale(locale);
        }
    }

    @Override
    public String convertToString(Postcode value, Locale locale) {
        return value.getValue();
    }
}
