package nl.rotterdam.verbonden.core.administration_common;

import jakarta.servlet.RequestDispatcher;
import nl.rotterdam.verbonden.core.burger_common.BurgerErrorPage;
import nl.rotterdam.verbonden.core.burger_common.BurgerPageExpiredPage;
import nl.rotterdam.verbonden.core.error_common.VerbondenExceptionMapper;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.apache.wicket.authorization.UnauthorizedActionException;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IRequestHandler;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;

@WithMockUser(username = "medewerker", roles = "BEHEERDER")
class AdministrationErrorPageTest extends BaseWicketTest {

    @Test
    void containerErrorInBeheerRequestShowsAdministrationErrorPage() {
        tester.getRequest().setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 404);
        tester.getRequest().setAttribute(RequestDispatcher.ERROR_REQUEST_URI,
                tester.getRequest().getContextPath() + "/beheer/bestaat-niet");

        tester.startPage(BurgerErrorPage.class);

        tester.assertRenderedPage(AdministrationErrorPage.class);
        assertErrorPage(404, "Pagina niet gevonden");
    }

    @Test
    void containerErrorOutsideBeheerRequestShowsBurgerErrorPage() {
        tester.getRequest().setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 404);
        tester.getRequest().setAttribute(RequestDispatcher.ERROR_REQUEST_URI,
                tester.getRequest().getContextPath() + "/beheerder");

        tester.startPage(BurgerErrorPage.class);

        tester.assertRenderedPage(BurgerErrorPage.class);
    }

    @Test
    void internalErrorPage() {
        tester.startPage(AdministrationInternalErrorPage.class);

        assertErrorPage(500, "Er is een interne fout opgetreden");
    }

    @Test
    void accessDeniedPage() {
        tester.startPage(AdministrationAccessDeniedPage.class);

        assertErrorPage(403, "Toegang geweigerd");
    }

    @Test
    void pageExpiredPage() {
        tester.startPage(AdministrationPageExpiredPage.class);

        assertErrorPage(410, "De pagina is verlopen. Ga terug naar beheer en probeer het opnieuw.");
    }

    @Test
    void exceptionMapperChoosesAdministrationErrorPageInBeheerRequest() {
        tester.getRequest().setURL("/beheer/huwelijkstypen");

        assertThat(mappedPageClass(new PageExpiredException("verlopen"))).isEqualTo(AdministrationPageExpiredPage.class);
        assertThat(mappedPageClass(new UnauthorizedActionException(new WebPage() {
        }, WebPage.ENABLE))).isEqualTo(AdministrationAccessDeniedPage.class);
    }

    @Test
    void exceptionMapperKeepsBurgerErrorPageOutsideBeheerRequest() {
        tester.getRequest().setURL("/mijn-dag/1");

        assertThat(mappedPageClass(new PageExpiredException("verlopen"))).isEqualTo(BurgerPageExpiredPage.class);
    }

    private Class<?> mappedPageClass(Exception e) {
        IRequestHandler handler = new VerbondenExceptionMapper().map(e);
        return ((RenderPageRequestHandler) handler).getPageClass();
    }

    private void assertErrorPage(int errorCode, String errorMessage) {
        assertThat(tester.getLastResponse().getStatus()).isEqualTo(errorCode);
        tester.assertContains("<strong>" + errorCode + "</strong>");
        tester.assertContains("<p>" + errorMessage + "</p>");
        tester.assertContains("href=\"/logout\"");
    }
}
