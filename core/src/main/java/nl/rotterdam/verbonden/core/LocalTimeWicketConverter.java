package nl.rotterdam.verbonden.core;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class LocalTimeWicketConverter implements IConverter<LocalTime> {

    private static final DateTimeFormatter PARSER =
            new DateTimeFormatterBuilder()
                    .appendPattern("HH:mm")
                    .optionalStart().appendPattern(":ss").optionalEnd()
                    .toFormatter();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public LocalTime convertToObject(String value, Locale locale) throws ConversionException {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(value, PARSER);
        } catch (DateTimeParseException e) {
            throw new ConversionException(e).setResourceKey("IConverter.Time");
        }
    }

    @Override
    public String convertToString(LocalTime value, Locale locale) {
        return value != null ? value.format(FORMATTER) : "";
    }
}
