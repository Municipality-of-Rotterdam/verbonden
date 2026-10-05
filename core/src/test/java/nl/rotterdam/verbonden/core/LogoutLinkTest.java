package nl.rotterdam.verbonden.core;

import nl.rotterdam.verbonden.core.features.babs_administration.ui.BabsAdministrationPage;
import nl.rotterdam.verbonden.core.features.marriage_intake.ui.MarriageIntakePage;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * De Uitloggen-links komen uit {@code verbonden.logout.burger-url} en {@code verbonden.logout.beheer-url},
 * zie {@code @VerbondenIntegrationTest}.
 */
class LogoutLinkTest extends BaseWicketTest {

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void burgerPageLinksToConfiguredLogoutUrl() {
        tester.startPage(MarriageIntakePage.class);

        tester.assertContains("href=\"/uitloggen\"");
    }

    @Test
    @WithMockUser(roles = "BEHEERDER")
    void beheerPageLinksToConfiguredLogoutUrl() {
        tester.startPage(BabsAdministrationPage.class);

        tester.assertContains("href=\"/logout\"");
    }
}
