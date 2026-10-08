package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.domain.BuitenlandsPersoonsnummer;
import nl.rotterdam.verbonden.core.domain.BuitenlandsPersoonsnummerOngeldigException;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.util.Locale;

public class BuitenlandsPersoonsnummerWicketConverter implements IConverter<BuitenlandsPersoonsnummer> {

    @Override
    public BuitenlandsPersoonsnummer convertToObject(String value, Locale locale) throws ConversionException {
        try {
            return new BuitenlandsPersoonsnummer(value);
        } catch (BuitenlandsPersoonsnummerOngeldigException e) {
            throw new ConversionException("Persoonsnummer is ongeldig")
                    .setResourceKey("BuitenlandsPersoonsnummerValidator")
                    .setLocale(locale);
        }
    }

    @Override
    public String convertToString(BuitenlandsPersoonsnummer value, Locale locale) {
        return value.getValue();
    }
}
