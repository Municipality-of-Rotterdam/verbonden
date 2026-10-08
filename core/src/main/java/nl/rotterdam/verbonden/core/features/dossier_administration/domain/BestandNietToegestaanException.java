package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

/**
 * Een bestand mag niet aan een dossier worden toegevoegd: het soort bestand is niet toegestaan, de inhoud past
 * niet bij de extensie, of het is te groot. De melding is geschikt om aan de medewerker te tonen.
 */
public class BestandNietToegestaanException extends RuntimeException {

    public BestandNietToegestaanException(String message) {
        super(message);
    }
}
