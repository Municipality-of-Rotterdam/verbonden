package nl.rotterdam.huwelijk.features.baps_administration.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value type representing a person's full name (first name + last name stored as a single string).
 * <p>
 * Validates that the value is more than 4 characters and less than 80 characters.
 * Throws {@link PersonFullNameTooShortException} or {@link PersonFullNameTooLongException}
 * when the input does not meet these constraints.
 */
public final class PersonFullName implements ValueHolder<String>, Serializable {

    private final String value;

    public PersonFullName(String value) {
        Objects.requireNonNull(value, "Naam mag niet null zijn");
        if (value.length() <= 4) {
            throw new PersonFullNameTooShortException(value);
        }
        if (value.length() >= 80) {
            throw new PersonFullNameTooLongException(value);
        }
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "PersonFullName[" + value + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonFullName that = (PersonFullName) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
