package nl.rotterdam.verbonden.core.domain;

import java.io.Serializable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Value type voor een postcode uit elk land: {@value #MAXIMALE_LENGTE} tekens of minder, met letters, cijfers,
 * spaties en '-'. Letters worden hoofdletters en dubbele spaties worden één spatie. Een Nederlandse postcode wordt
 * opgeslagen als {@code 1234 AB}; daar mag de spatie in de invoer ontbreken.
 */
public record Postcode(String value) implements ValueHolder<String>, Serializable {

    public static final int MAXIMALE_LENGTE = 10;

    private static final Pattern POSTCODE = Pattern.compile("[A-Z0-9][A-Z0-9 -]*");

    /** Een Nederlandse postcode begint nooit met 0 en eindigt nooit op SA, SD of SS. */
    private static final Pattern NEDERLANDSE_POSTCODE = Pattern.compile(
            "(?<cijfers>[1-9][0-9]{3}) ?(?<letters>(?!SA|SD|SS)[A-Z]{2})");

    public Postcode {
        requireNonNull(value, "Postcode mag niet null zijn");

        String genormaliseerd = value.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        Matcher nederlands = NEDERLANDSE_POSTCODE.matcher(genormaliseerd);
        if (nederlands.matches()) {
            genormaliseerd = nederlands.group("cijfers") + " " + nederlands.group("letters");
        }
        if (genormaliseerd.length() > MAXIMALE_LENGTE || !POSTCODE.matcher(genormaliseerd).matches()) {
            throw new PostcodeOngeldigException(value);
        }
        value = genormaliseerd;
    }

    /**
     * Of dit een geldige Nederlandse postcode is.
     */
    public boolean isNederlands() {
        return NEDERLANDSE_POSTCODE.matcher(value).matches();
    }

    @Override
    public String getValue() {
        return value;
    }
}
