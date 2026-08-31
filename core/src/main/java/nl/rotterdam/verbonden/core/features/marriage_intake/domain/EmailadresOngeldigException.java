package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

public class EmailadresOngeldigException extends RuntimeException {

    public EmailadresOngeldigException(String value) {
        super("Emailadres is ongeldig: '" + value + "'");
    }
}
