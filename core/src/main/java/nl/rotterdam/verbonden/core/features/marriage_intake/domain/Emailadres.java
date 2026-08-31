package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.ValueHolder;
import org.apache.commons.validator.routines.EmailValidator;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

/**
 * Value type representing an email address.
 * Validated using Apache Commons EmailValidator.
 */
public record Emailadres(String value) implements ValueHolder<String>, Serializable {

    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();

    public Emailadres {
        requireNonNull(value, "Emailadres mag niet null zijn");
        value = value.trim();

        if (!EMAIL_VALIDATOR.isValid(value)) {
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
