package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

/**
 * Deze betaalmutatie valt (niet meer) te betalen: hij is al betaald, intussen verwijderd, of het dossier kent geen
 * betaalmutaties (meer). Toon de burger de actuele stand.
 */
public class BetaalmutatieNietTeBetalenException extends RuntimeException {

    public BetaalmutatieNietTeBetalenException(String message) {
        super(message);
    }
}
