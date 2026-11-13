package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

/**
 * Er loopt al een betaling waarvan de bank de uitslag nog niet heeft gemeld. Een tweede betaling starten zou
 * dubbel betalen kunnen opleveren; laat de burger wachten op de uitslag.
 */
public class BetalingLooptAlException extends RuntimeException {

    public BetalingLooptAlException(String message) {
        super(message);
    }
}
