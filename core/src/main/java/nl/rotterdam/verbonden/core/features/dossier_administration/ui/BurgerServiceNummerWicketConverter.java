package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummerOngeldigException;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.util.Locale;

public class BurgerServiceNummerWicketConverter implements IConverter<BurgerServiceNummer> {

    @Override
    public BurgerServiceNummer convertToObject(String value, Locale locale) throws ConversionException {
        try {
            return new BurgerServiceNummer(value.strip());
        } catch (BurgerServiceNummerOngeldigException e) {
            throw new ConversionException("BSN is ongeldig")
                    .setResourceKey("BurgerServiceNummerValidator")
                    .setLocale(locale);
        }
    }

    @Override
    public String convertToString(BurgerServiceNummer value, Locale locale) {
        return value.getValue();
    }
}
