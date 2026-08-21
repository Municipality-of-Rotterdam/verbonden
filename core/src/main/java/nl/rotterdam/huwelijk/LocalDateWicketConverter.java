package nl.rotterdam.huwelijk;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class LocalDateWicketConverter implements IConverter<LocalDate> {

    @Override
    public LocalDate convertToObject(String value, Locale locale) throws ConversionException {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new ConversionException(e).setResourceKey("IConverter.Date");
        }
    }

    @Override
    public String convertToString(LocalDate value, Locale locale) {
        return value != null ? value.format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
    }
}
