package nl.rotterdam.verbonden.core.domain;

public class PersonFullNameTooLongException extends RuntimeException {

    public PersonFullNameTooLongException(String value) {
        super("Naam is te lang: " + value.length() + " tekens (maximaal " + PersonFullName.MAXIMUM_LENGTH  + " tekens toegestaan)");
    }
}
