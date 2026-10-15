package nl.rotterdam.verbonden.core.burger_common;

import nl.rotterdam.verbonden.core.administration_common.AdministrationErrorPage;
import nl.rotterdam.verbonden.core.error_common.ServletErrorAttributes;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.request.http.WebResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Foutpagina voor fouten die de servlet container afhandelt (bijv. 404), gemount op {@code /error}.
 * Statuscode en melding komen uit de {@code jakarta.servlet.error.*} request-attributen. Een fout in een
 * {@code /beheer}-request wordt als {@link AdministrationErrorPage} getoond: de container kent maar één
 * foutpagina, en een burgerpagina werkt niet voor een medewerker.
 * Subklassen geven voor Wicket-fouten zelf een statuscode en melding mee.
 */
public class BurgerErrorPage extends BurgerBasePage {

    private static final Logger log = LoggerFactory.getLogger(BurgerErrorPage.class);

    private final int errorCode;
    private final String errorMessage;

    public BurgerErrorPage() {
        if (ServletErrorAttributes.isAdministrationRequest()) {
            // Niet redirecten: dan gaan statuscode en request-attributen verloren.
            throw new RestartResponseException(new PageProvider(AdministrationErrorPage.class), RedirectPolicy.NEVER_REDIRECT);
        }
        this.errorCode = ServletErrorAttributes.resolveErrorCode();
        this.errorMessage = ServletErrorAttributes.resolveErrorMessage(errorCode);
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

        Throwable cause = ServletErrorAttributes.resolveException();
        String technicalMessage = ServletErrorAttributes.resolveTechnicalMessage();
        if (errorCode >= 400 && errorCode < 500) {
            log.warn("Error page displayed [{} {}] {}", errorCode, errorMessage, technicalMessage, cause);
        } else {
            log.error("Error page displayed [{} {}] {}", errorCode, errorMessage, technicalMessage, cause);
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
}
