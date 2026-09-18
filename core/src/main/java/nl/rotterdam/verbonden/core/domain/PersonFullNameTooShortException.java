package nl.rotterdam.verbonden.core.domain;

public class PersonFullNameTooShortException extends RuntimeException {

    public PersonFullNameTooShortException(String value) {
        super("Naam is te kort: '" + value + "' (minimaal " + PersonFullName.MINIMUM_LENGTH + " tekens vereist)");
    }
}
