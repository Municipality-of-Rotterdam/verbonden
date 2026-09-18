package nl.rotterdam.verbonden.core.domain;

public class TelefoonnummerOngeldigException extends RuntimeException {

    public TelefoonnummerOngeldigException(String value) {
        super("Telefoonnummer is ongeldig: '" + value + "'");
    }
}
