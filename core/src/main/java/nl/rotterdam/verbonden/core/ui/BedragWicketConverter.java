package nl.rotterdam.verbonden.core.ui;

import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.domain.BedragTeVeelDecimalenException;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.math.BigDecimal;
import java.util.Locale;

public class BedragWicketConverter implements IConverter<Bedrag> {

    @Override
    public Bedrag convertToObject(String value, Locale locale) throws ConversionException {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new Bedrag(new BigDecimal(value.trim().replace(',', '.')));
        } catch (NumberFormatException | BedragTeVeelDecimalenException e) {
            throw new ConversionException(e).setResourceKey("IConverter.Number");
        }
    }

    @Override
    public String convertToString(Bedrag value, Locale locale) {
        return value != null ? value.getValue().toPlainString() : "";
    }
}
