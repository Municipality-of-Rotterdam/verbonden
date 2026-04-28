package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import nl.rotterdam.huwelijk.domain.ValueHolder;

import java.io.Serializable;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Value type representing an email address.
 * Accepts common email formats: local-part@domain.tld.
 */
public record Emailadres(String value) implements ValueHolder<String>, Serializable {

    private static final Pattern PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s.][^@\\s]*\\.[^@\\s.][^@\\s]*$");

    public Emailadres {
        requireNonNull(value, "Emailadres mag niet null zijn");
        value = value.trim();

        if (!PATTERN.matcher(value).matches()) {
            throw new EmailadresOngeldigException(value);
        }
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Emailadres[" + value + "]";
    }
}
