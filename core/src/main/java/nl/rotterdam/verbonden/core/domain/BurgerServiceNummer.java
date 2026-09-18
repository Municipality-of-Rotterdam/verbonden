package nl.rotterdam.verbonden.core.domain;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

/**
 * Value type representing a Burgerservicenummer (BSN).
 * Accepts input with fewer than {@value LENGTH} digits and left-pads it with
 * zeros, since the number of leading zeros in a BSN is not fixed.
 */
public record BurgerServiceNummer(String value) implements ValueHolder<String>, Serializable {

    public static final int LENGTH = 9;

    public BurgerServiceNummer {
        requireNonNull(value, "BSN mag niet null zijn");

        if (value.isBlank()) {
            throw new BurgerServiceNummerOngeldigException("BSN mag niet leeg zijn");
        }
        if (!value.chars().allMatch(Character::isDigit)) {
            throw new BurgerServiceNummerOngeldigException("BSN is niet numeriek: '" + value + "'");
        }
        if (value.length() > LENGTH) {
            throw new BurgerServiceNummerOngeldigException(
                    "BSN mag niet meer dan " + LENGTH + " cijfers bevatten: '" + value + "'");
        }

        value = "0".repeat(LENGTH - value.length()) + value;

        if (!voldoetAanElfproef(value)) {
            throw new BurgerServiceNummerOngeldigException("BSN voldoet niet aan de elfproef: '" + value + "'");
        }
    }

    @Override
    public String getValue() {
        return value;
    }

    private static boolean voldoetAanElfproef(String bsn) {
        int som = 0;
        for (int i = 0; i < LENGTH; i++) {
            int cijfer = Character.getNumericValue(bsn.charAt(i));
            int gewicht = i == LENGTH - 1 ? -1 : LENGTH - i;
            som += gewicht * cijfer;
        }
        return som % 11 == 0;
    }
}
