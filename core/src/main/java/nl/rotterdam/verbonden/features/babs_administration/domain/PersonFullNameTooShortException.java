package nl.rotterdam.verbonden.features.babs_administration.domain;

public class PersonFullNameTooShortException extends RuntimeException {

    public PersonFullNameTooShortException(String value) {
        super("Naam is te kort: '" + value + "' (minimaal " + PersonFullName.MINIMUM_LENGTH + " tekens vereist)");
    }
}
