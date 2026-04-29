package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import nl.rotterdam.huwelijk.domain.ValueHolder;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

/**
 * Value type representing a phone number.
 * NL numbers are assumed when no country code is given (e.g. "06-12345678", "0201234567").
 * Full international numbers with a country code prefix (e.g. "+32 470 123456") are also accepted.
 */
public record Telefoonnummer(String value) implements ValueHolder<String>, Serializable {

    private static final PhoneNumberUtil PHONE_UTIL = PhoneNumberUtil.getInstance();

    public Telefoonnummer {
        requireNonNull(value, "Telefoonnummer mag niet null zijn");
        value = value.trim();

        try {
            var number = PHONE_UTIL.parse(value, "NL");
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
}
