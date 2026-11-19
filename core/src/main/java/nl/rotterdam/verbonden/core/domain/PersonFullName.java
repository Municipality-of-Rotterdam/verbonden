package nl.rotterdam.verbonden.core.domain;

import java.io.Serializable;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Value type representing a person's full name (first name + last name stored as a single string).
 */
public record PersonFullName(String value) implements ValueHolder<String>, Serializable {

    public static final int MINIMUM_LENGTH = 5;
    public static final int MAXIMUM_LENGTH = 80;
    private static final Pattern VALID_NAME = Pattern.compile("[\\p{L} .'-]+");

    public PersonFullName {
        requireNonNull(value, "Naam mag niet null zijn");
        value = value.trim();

        if (!value.isEmpty() && !VALID_NAME.matcher(value).matches()) {
            throw new PersonFullNameInvalidException(value);
        }
        if (value.length() < MINIMUM_LENGTH) {
            throw new PersonFullNameTooShortException(value);
        }
        if (value.length() >= MAXIMUM_LENGTH) {
            throw new PersonFullNameTooLongException(value);
        }
    }

    @Override
    public String getValue() {
        return value;
    }

}
