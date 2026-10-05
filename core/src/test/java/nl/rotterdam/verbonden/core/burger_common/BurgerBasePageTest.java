package nl.rotterdam.verbonden.core.burger_common;

import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import nl.rotterdam.verbonden.core.integration_test.TestPersonLookupService;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.visit.IVisit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Header van {@link BurgerBasePage}, gerenderd via een foutpagina zodat alleen de header
 * de {@code PersonLookupService} aanroept.
 */
class BurgerBasePageTest extends BaseWicketTest {

    @Autowired
    private TestPersonLookupService personLookupService;

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void headerShowsOfficieleNaamInsteadOfBsn() {
        tester.startPage(BurgerInternalErrorPage.class);

        assertThat(userName()).isEqualTo("E.J. van Muiswinkel");
    }

    @Test
    @WithMockUser(username = "999990019", roles = "BURGER")
    void officieleNaamIsLookedUpOncePerSession() {
        int lookupsBefore = personLookupService.getLookupCount("999990019");

        tester.startPage(BurgerInternalErrorPage.class);
        tester.startPage(BurgerAccessDeniedPage.class);

        assertThat(userName()).isEqualTo("S.M. de Vries");
        assertThat(personLookupService.getLookupCount("999990019")).isEqualTo(lookupsBefore + 1);
    }

    @Test
    @WithMockUser(username = TestPersonLookupService.ONBEREIKBAAR_BSN, roles = "BURGER")
    void pageStillRendersWhenLookupFails() {
        tester.startPage(BurgerInternalErrorPage.class);

        tester.assertRenderedPage(BurgerInternalErrorPage.class);
        assertThat(userName()).isEmpty();
    }

    /**
     * Een medewerker heeft geen BSN; de header mag de gebruikersnaam dan niet als BSN opvatten.
     */
    @Test
    @WithMockUser(username = "medewerker", roles = "BEHEERDER")
    void headerHidesUserBarForNonBurger() {
        tester.startPage(BurgerInternalErrorPage.class);

        tester.assertRenderedPage(BurgerInternalErrorPage.class);
        tester.assertContainsNot("href=\"/uitloggen\"");
    }

    private String userName() {
        Component userName = tester.getLastRenderedPage().visitChildren(Label.class, (Label label, IVisit<Component> visit) -> {
            if ("userName".equals(label.getId())) {
                visit.stop(label);
            }
        });
        return userName.getDefaultModelObjectAsString();
    }
}
