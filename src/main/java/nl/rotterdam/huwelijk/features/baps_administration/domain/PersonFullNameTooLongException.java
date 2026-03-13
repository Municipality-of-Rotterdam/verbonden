package nl.rotterdam.huwelijk.features.baps_administration.domain;

public class PersonFullNameTooLongException extends RuntimeException {

    public PersonFullNameTooLongException(String value) {
        super("Naam is te lang: " + value.length() + " tekens (maximaal " + PersonFullName.MAXIMUM_LENGTH  + " tekens toegestaan)");
    }
}
