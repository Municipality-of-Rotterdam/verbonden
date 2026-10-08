package nl.rotterdam.verbonden.core.domain;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

/**
 * Value type voor het persoonsnummer van een partner zonder BSN, zoals dat in het buitenlandse paspoort of
 * identiteitsbewijs staat (bijvoorbeeld een social security number). Het formaat verschilt per land, dus
 * alleen lengte en tekenset worden gecontroleerd. Spaties aan het begin en einde worden verwijderd.
 */
public record BuitenlandsPersoonsnummer(String value) implements ValueHolder<String>, Serializable {

    public static final int MAX_LENGTE = 50;

    public BuitenlandsPersoonsnummer {
        requireNonNull(value, "Persoonsnummer mag niet null zijn");
        value = value.strip();

        if (value.isEmpty()) {
            throw new BuitenlandsPersoonsnummerOngeldigException("Persoonsnummer mag niet leeg zijn");
        }
        if (value.length() > MAX_LENGTE) {
            throw new BuitenlandsPersoonsnummerOngeldigException(
                    "Persoonsnummer mag niet meer dan " + MAX_LENGTE + " tekens bevatten");
        }
        if (!value.chars().allMatch(c -> Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '.' || c == '/')) {
            throw new BuitenlandsPersoonsnummerOngeldigException(
                    "Persoonsnummer mag alleen letters, cijfers, spaties, '-', '.' en '/' bevatten: '" + value + "'");
        }
    }

    @Override
    public String getValue() {
        return value;
    }
}
