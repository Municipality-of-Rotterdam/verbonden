package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

/**
 * Een betaalmutatie kan nu niet worden toegevoegd of verwijderd; de melding is geschikt voor de medewerker.
 */
public class BetaalmutatieNietMogelijkException extends RuntimeException {

    public BetaalmutatieNietMogelijkException(String message) {
        super(message);
    }
}
