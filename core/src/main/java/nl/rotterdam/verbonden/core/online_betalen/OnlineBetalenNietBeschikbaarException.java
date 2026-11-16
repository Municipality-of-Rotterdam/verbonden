package nl.rotterdam.verbonden.core.online_betalen;

/**
 * De betaalprovider kan nu geen betaling aanmaken. De burger kan het later opnieuw proberen.
 */
public class OnlineBetalenNietBeschikbaarException extends RuntimeException {

    public OnlineBetalenNietBeschikbaarException(String message, Throwable cause) {
        super(message, cause);
    }
}
