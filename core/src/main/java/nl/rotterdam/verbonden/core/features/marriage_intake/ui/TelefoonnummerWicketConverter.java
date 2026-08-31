package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.Telefoonnummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.TelefoonnummerOngeldigException;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.util.Locale;

public class TelefoonnummerWicketConverter implements IConverter<Telefoonnummer> {

    @Override
    public Telefoonnummer convertToObject(String value, Locale locale) throws ConversionException {
        try {
            return new Telefoonnummer(value);
        } catch (TelefoonnummerOngeldigException e) {
            throw new ConversionException("Telefoonnummer is ongeldig")
                    .setResourceKey("TelefoonnummerValidator")
                    .setLocale(locale);
        }
    }

    @Override
    public String convertToString(Telefoonnummer value, Locale locale) {
        return  value.getValue();
    }
}
