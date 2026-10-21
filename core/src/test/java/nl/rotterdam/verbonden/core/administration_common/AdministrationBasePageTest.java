package nl.rotterdam.verbonden.core.administration_common;

import nl.rotterdam.verbonden.core.features.babs_administration.ui.BabsAdministrationPage;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;

class AdministrationBasePageTest extends BaseWicketTest {

    @Test
    @WithMockUser(roles = Roles.BEHEER)
    void toontSidebarMetBeheermenuEnZonderBovenOfOnderbalk() {
        tester.startPage(BabsAdministrationPage.class);

        String response = tester.getLastResponseAsString();
        assertThat(response)
                .contains("administration-sidebar-toggle")
                .contains("BABS")
                .contains("Trouwlocaties")
                .contains("Huwelijkstypen")
                .contains("Dossiers")
                .contains("Trouwboekjes")
                .contains("Uitloggen")
                .contains(">user<")
                .contains("href=\"/logout\"")
                .doesNotContain("rd-top-bar")
                .doesNotContain("rd-footer-links")
                .doesNotContain("pageHeader")
                .doesNotContain("pageFooter");
    }
}
