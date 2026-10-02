package nl.rotterdam.verbonden.core.burger_common;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Foutpagina voor fouten die de servlet container afhandelt (bijv. 404), gemount op {@code /error}.
 * Statuscode en melding komen uit de {@code jakarta.servlet.error.*} request-attributen.
 * Subklassen geven voor Wicket-fouten zelf een statuscode en melding mee.
 */
public class BurgerErrorPage extends BurgerBasePage {

    private static final Logger log = LoggerFactory.getLogger(BurgerErrorPage.class);

    private final int errorCode;
    private final String errorMessage;

    public BurgerErrorPage() {
        this.errorCode = resolveErrorCode();
        this.errorMessage = resolveErrorMessage(errorCode);
    }

    protected BurgerErrorPage(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return Model.of("Er is een fout opgetreden");
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        Throwable cause = resolveException();
        if (errorCode >= 400 && errorCode < 500) {
            log.warn("Error page displayed [{} {}]", errorCode, errorMessage, cause);
        } else {
            log.error("Error page displayed [{} {}]", errorCode, errorMessage, cause);
        }

        pageBody.add(new Label("errorCode", String.valueOf(errorCode)));
        pageBody.add(new Label("errorMessage", errorMessage));
    }

    @Override
    protected void configureResponse(WebResponse response) {
        super.configureResponse(response);
        response.setStatus(errorCode);
    }

    @Override
    public boolean isErrorPage() {
        return true;
    }

    @Override
    public boolean isVersioned() {
        return false;
    }

    private static int resolveErrorCode() {
        Integer statusCode = servletAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        return statusCode != null ? statusCode : 500;
    }

    private static String resolveErrorMessage(int errorCode) {
        String message = servletAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (message != null && !message.isBlank()) {
            return message;
        }
        Throwable ex = resolveException();
        if (ex != null && ex.getMessage() != null && !ex.getMessage().isBlank()) {
            return ex.getMessage();
        }
        return switch (errorCode) {
            case 400 -> "Ongeldig verzoek";
            case 403 -> "Toegang geweigerd";
            case 404 -> "Pagina niet gevonden";
            default -> "Er is een interne fout opgetreden";
        };
    }

    private static Throwable resolveException() {
        return servletAttribute(RequestDispatcher.ERROR_EXCEPTION);
    }

    @SuppressWarnings("unchecked")
    private static <T> T servletAttribute(String name) {
        try {
            HttpServletRequest req = (HttpServletRequest)
                    RequestCycle.get().getRequest().getContainerRequest();
            return (T) req.getAttribute(name);
        } catch (Exception e) {
            return null;
        }
    }
}
