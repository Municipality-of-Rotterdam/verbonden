package nl.rotterdam.verbonden.core.domain;

public class PostcodeOngeldigException extends RuntimeException {

    public PostcodeOngeldigException(String value) {
        super("Postcode is ongeldig: '" + value + "'");
    }
}
