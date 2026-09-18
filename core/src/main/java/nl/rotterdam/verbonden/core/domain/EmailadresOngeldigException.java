package nl.rotterdam.verbonden.core.domain;

public class EmailadresOngeldigException extends RuntimeException {

    public EmailadresOngeldigException(String value) {
        super("Emailadres is ongeldig: '" + value + "'");
    }
}
