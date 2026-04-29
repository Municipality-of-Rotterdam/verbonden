package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import jakarta.annotation.Nonnull;
import nl.rotterdam.huwelijk.domain.ValueHolder;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

/**
 * Value type representing a phone number.
 * NL numbers are assumed when no country code is given (e.g. "06-12345678", "0201234567").
 * Full international numbers with a country code prefix (e.g. "+32 470 12 34 56") are also accepted.
 * The raw trimmed input is preserved as the stored value.
 */
public record Telefoonnummer(String value) implements ValueHolder<String>, Serializable {

    private static final PhoneNumberUtil PHONE_UTIL = PhoneNumberUtil.getInstance();

    public Telefoonnummer {
        requireNonNull(value, "Telefoonnummer mag niet null zijn");
        value = value.trim();

        String forParsing = replacePrefix(value.replaceAll("[\\s-]", ""), "00", "+");
        try {
            var number = PHONE_UTIL.parse(forParsing, "NL");
            if (!PHONE_UTIL.isValidNumber(number)) {
                throw new TelefoonnummerOngeldigException(value);
            }
        } catch (NumberParseException e) {
            throw new TelefoonnummerOngeldigException(value);
        }
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Telefoonnummer[" + value + "]";
    }

    private static String replacePrefix(@Nonnull String value, String prefix, String replacement) {
        return value.startsWith(prefix) ? value.replaceFirst(prefix, replacement) : value;
    }
}
