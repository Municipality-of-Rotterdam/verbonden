package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

public class TelefoonnummerOngeldigException extends RuntimeException {

    public TelefoonnummerOngeldigException(String value) {
        super("Telefoonnummer is ongeldig: '" + value + "'");
    }
}
