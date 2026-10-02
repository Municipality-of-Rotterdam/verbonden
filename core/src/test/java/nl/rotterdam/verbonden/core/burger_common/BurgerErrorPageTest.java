package nl.rotterdam.verbonden.core.burger_common;

import jakarta.servlet.RequestDispatcher;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;

@WithMockUser(username = "999990007")
class BurgerErrorPageTest extends BaseWicketTest {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * De foutpagina moet ook zonder inloggen bereikbaar zijn; anders wordt een fout op een
     * publieke pagina (bijv. tijdens het inloggen) omgeleid naar {@code /inloggen}.
     */
    @Test
    void errorPageIsReachableWithoutLogin() {
        ResponseEntity<String> response = restTemplate.getForEntity("/error", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Er is een fout opgetreden");
    }

    @Test
    void containerErrorShowsStatusCodeFromRequestAttributes() {
        tester.getRequest().setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 404);

        tester.startPage(BurgerErrorPage.class);

        assertErrorPage(404, "Pagina niet gevonden");
    }

    @Test
    void internalErrorPage() {
        tester.startPage(BurgerInternalErrorPage.class);

        assertErrorPage(500, "Er is een interne fout opgetreden");
    }

    @Test
    void accessDeniedPage() {
        tester.startPage(BurgerAccessDeniedPage.class);

        assertErrorPage(403, "Toegang geweigerd");
    }

    @Test
    void pageExpiredPage() {
        tester.startPage(BurgerPageExpiredPage.class);

        assertErrorPage(410, "De pagina is verlopen. Ga terug naar de beginpagina en probeer het opnieuw.");
    }

    private void assertErrorPage(int errorCode, String errorMessage) {
        assertThat(tester.getLastResponse().getStatus()).isEqualTo(errorCode);
        tester.assertContains("<strong>" + errorCode + "</strong>");
        tester.assertContains("<p>" + errorMessage + "</p>");
    }
}
