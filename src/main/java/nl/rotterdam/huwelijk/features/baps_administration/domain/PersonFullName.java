package nl.rotterdam.huwelijk.features.baps_administration.domain;

import nl.rotterdam.huwelijk.domain.ValueHolder;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

/**
 * Value type representing a person's full name (first name + last name stored as a single string).
 */
public record PersonFullName(String value) implements ValueHolder<String>, Serializable {

    public static final int MINIMUM_LENGTH = 5;
    public static final int MAXIMUM_LENGTH = 80;

    public PersonFullName {
        requireNonNull(value, "Naam mag niet null zijn");
        value = value.trim();

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

    @Override
    public String toString() {
        return "PersonFullName[" + value + "]";
    }
}
