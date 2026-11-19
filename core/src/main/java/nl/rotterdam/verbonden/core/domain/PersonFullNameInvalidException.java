package nl.rotterdam.verbonden.core.domain;

public class PersonFullNameInvalidException extends RuntimeException {

    public PersonFullNameInvalidException(String value) {
        super("Naam bevat ongeldige tekens: '" + value + "'");
    }
}
