package nl.rotterdam.verbonden.core.error_common;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * Leest de foutgegevens van het huidige request uit, voor de burger- en beheerfoutpagina's.
 * Statuscode, melding en exceptie komen uit de {@code jakarta.servlet.error.*} request-attributen
 * die de servlet container zet bij een forward naar {@code /error}.
 */
public final class ServletErrorAttributes {

    private static final String BEHEER_PATH = "/beheer";

    private ServletErrorAttributes() {
    }

    public static int resolveErrorCode() {
        Integer statusCode = servletAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        return statusCode != null ? statusCode : 500;
    }

    /**
     * Melding voor de bezoeker. Bewust alleen op basis van de statuscode: de melding van de container of
     * exceptie is technisch en Engelstalig (bijv. "No static resource bestaat-niet.") en kan interne details
     * prijsgeven. Die staat in {@link #resolveTechnicalMessage()} en hoort alleen in de log.
     */
    public static String resolveErrorMessage(int errorCode) {
        return switch (errorCode) {
            case 400 -> "Ongeldig verzoek";
            case 403 -> "Toegang geweigerd";
            case 404 -> "Pagina niet gevonden. Misschien is het adres verkeerd getypt of bestaat de pagina niet meer.";
            default -> "Er is een interne fout opgetreden";
        };
    }

    /**
     * Technische melding van de servlet container, of anders van de exceptie; alleen voor de log.
     */
    public static String resolveTechnicalMessage() {
        String message = servletAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (message != null && !message.isBlank()) {
            return message;
        }
        Throwable ex = resolveException();
        return ex != null ? ex.getMessage() : null;
    }

    public static Throwable resolveException() {
        return servletAttribute(RequestDispatcher.ERROR_EXCEPTION);
    }

    /**
     * Of het (oorspronkelijke) request onder {@code /beheer} valt, net als de admin filter chain in
     * {@code SecurityConfig}. Bij een forward naar {@code /error} telt de oorspronkelijke URI.
     * Bewust op pad en niet op rol: een medewerker die via een gateway-header wordt herkend, is
     * dat alleen in de admin filter chain, niet op {@code /error}.
     */
    public static boolean isAdministrationRequest() {
        HttpServletRequest req = containerRequest();
        if (req == null) {
            return false;
        }
        String uri = req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI) instanceof String errorRequestUri
                ? errorRequestUri
                : req.getRequestURI();
        if (uri == null) {
            return false;
        }
        String path = uri.startsWith(req.getContextPath()) ? uri.substring(req.getContextPath().length()) : uri;
        return path.equals(BEHEER_PATH) || path.startsWith(BEHEER_PATH + "/");
    }

    @SuppressWarnings("unchecked")
    private static <T> T servletAttribute(String name) {
        HttpServletRequest req = containerRequest();
        return req != null ? (T) req.getAttribute(name) : null;
    }

    private static HttpServletRequest containerRequest() {
        try {
            return (HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest();
        } catch (Exception e) {
            return null;
        }
    }
}
