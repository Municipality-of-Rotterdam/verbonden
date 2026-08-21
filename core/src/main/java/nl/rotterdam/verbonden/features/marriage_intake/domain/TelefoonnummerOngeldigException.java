package nl.rotterdam.verbonden.features.marriage_intake.domain;

public class TelefoonnummerOngeldigException extends RuntimeException {

    public TelefoonnummerOngeldigException(String value) {
        super("Telefoonnummer is ongeldig: '" + value + "'");
    }
}
