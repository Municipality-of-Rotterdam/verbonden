package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.domain.Telefoonnummer;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.TelefoonnummerOngeldigException;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public class TelefoonnummerWicketConverter implements IConverter<Telefoonnummer> {

    @Override
    public @Nullable Telefoonnummer convertToObject(String value, Locale locale) throws ConversionException {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new Telefoonnummer(value);
        } catch (TelefoonnummerOngeldigException e) {
            throw new ConversionException("Telefoonnummer is ongeldig")
                    .setResourceKey("Telefoonnummer.ongeldig")
                    .setLocale(locale);
        }
    }

    @Override
    public String convertToString(Telefoonnummer value, Locale locale) {
        return value.getValue();
    }
}
