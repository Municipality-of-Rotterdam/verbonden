package nl.rotterdam.verbonden.core.domain;

import java.io.Serializable;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Value type representing a person's full name (first name + last name stored as a single string).
 */
public record PersonFullName(String value) implements ValueHolder<String>, Serializable {

    public static final int MINIMUM_LENGTH = 2;
    public static final int MAXIMUM_LENGTH = 80;
    // Letters en combinerende tekens (zoals Arabische klinkertekens) uit elk schrift, plus spatie, punt,
    // apostrof (ook ’ zoals in O’Connor), streepje en het Japanse middenpunt (zoals in ジョン・スミス).
    private static final Pattern VALID_NAME = Pattern.compile("[\\p{L}\\p{M} .'’・-]+");
    private static final Pattern LETTER = Pattern.compile("\\p{L}");

    public PersonFullName {
        requireNonNull(value, "Naam mag niet null zijn");
        value = value.trim();

        if (!value.isEmpty() && (!VALID_NAME.matcher(value).matches() || !LETTER.matcher(value).find())) {
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
