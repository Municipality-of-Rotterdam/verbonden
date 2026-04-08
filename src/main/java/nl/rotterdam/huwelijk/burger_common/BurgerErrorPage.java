package nl.rotterdam.huwelijk.burger_common;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BurgerErrorPage extends BurgerBasePage {

    private static final Logger log = LoggerFactory.getLogger(BurgerErrorPage.class);

    @Override
    protected IModel<String> getTitleModel() {
        return Model.of("Er is een fout opgetreden");
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        int errorCode = resolveErrorCode();
        String errorMessage = resolveErrorMessage(errorCode);
        Throwable cause = resolveException();

        if (cause != null) {
            log.error("Error page displayed [{} {}]", errorCode, errorMessage, cause);
        } else {
            log.error("Error page displayed [{} {}]", errorCode, errorMessage);
        }

        pageBody.add(new Label("errorCode", String.valueOf(errorCode)));
        pageBody.add(new Label("errorMessage", errorMessage));
    }

    private int resolveErrorCode() {
        Integer statusCode = servletAttribute("javax.servlet.error.status_code");
        return statusCode != null ? statusCode : 500;
    }

    private String resolveErrorMessage(int errorCode) {
        String message = servletAttribute("javax.servlet.error.message");
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

    private Throwable resolveException() {
        return servletAttribute("javax.servlet.error.exception");
    }

    @SuppressWarnings("unchecked")
    private <T> T servletAttribute(String name) {
        try {
            HttpServletRequest req = (HttpServletRequest)
                    ((WebRequest) RequestCycle.get().getRequest()).getContainerRequest();
            return (T) req.getAttribute(name);
        } catch (Exception e) {
            return null;
        }
    }
}
