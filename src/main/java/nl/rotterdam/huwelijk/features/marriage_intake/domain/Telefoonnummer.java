package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import nl.rotterdam.huwelijk.domain.ValueHolder;

import java.io.Serializable;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Value type representing a Dutch phone number.
 * Accepts formats like 06-12345678, 0201234567, +31612345678, +31201234567.
 * Spaces and dashes used as separators are accepted.
 */
public record Telefoonnummer(String value) implements ValueHolder<String>, Serializable {

    private static final Pattern PATTERN = Pattern.compile("^(\\+31|0)[1-9][0-9]{8}$");

    public Telefoonnummer {
        requireNonNull(value, "Telefoonnummer mag niet null zijn");
        value = value.trim();

        String normalized = value.replaceAll("[\\s\\-]", "");
        if (!PATTERN.matcher(normalized).matches()) {
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
