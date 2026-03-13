package nl.rotterdam.huwelijk.features.baps_administration.domain;

public class PersonFullNameTooShortException extends RuntimeException {

    public PersonFullNameTooShortException(String value) {
        super("Naam is te kort: '" + value + "' (minimaal " + PersonFullName.MAXIMUM_LENGTH + " tekens vereist)");
    }
}
