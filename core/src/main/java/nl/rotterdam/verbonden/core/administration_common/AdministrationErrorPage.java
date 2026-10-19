package nl.rotterdam.verbonden.core.administration_common;

import nl.rotterdam.verbonden.core.error_common.ServletErrorAttributes;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.http.WebResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Foutpagina voor medewerkers. Niet gemount: {@code BurgerErrorPage} (op {@code /error}) en
 * {@code VerbondenExceptionMapper} sturen fouten in een {@code /beheer}-request hierheen.
 * Subklassen geven voor Wicket-fouten zelf een statuscode en melding mee.
 *
 * <p>Zonder rollen in {@link AuthorizeInstantiation} mag iedereen deze pagina zien, ook zonder toegang tot de
 * opgevraagde pagina (anders kan {@link AdministrationAccessDeniedPage} zelf niet getoond worden).
 */
@AuthorizeInstantiation
public class AdministrationErrorPage extends AdministrationBasePage {

    private static final Logger log = LoggerFactory.getLogger(AdministrationErrorPage.class);

    private final int errorCode;
    private final String errorMessage;

    public AdministrationErrorPage() {
        this.errorCode = ServletErrorAttributes.resolveErrorCode();
        this.errorMessage = ServletErrorAttributes.resolveErrorMessage(errorCode);
    }

    protected AdministrationErrorPage(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
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
