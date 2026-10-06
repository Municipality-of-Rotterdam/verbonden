package nl.rotterdam.verbonden.core.burger_common;

import nl.rotterdam.verbonden.core.features.marriage_intake.ui.MarriageIntakePage;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * De breadcrumb op burgerpagina's begint met een link naar Mijn Loket ({@code verbonden.mijn-loket-url}).
 */
class MijnLoketBreadcrumbTest extends BaseWicketTest {

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void zonderInstelling_linktNaarStandaardUrl() {
        tester.startPage(MarriageIntakePage.class);

        tester.assertContains("href=\"/mijnloket\">Mijn Loket</a>");
        tester.assertContains("utrecht-breadcrumb-nav__list");
        tester.assertContains("@utrecht/breadcrumb-nav-css/dist/index");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void andereHost_linktNaarIngesteldeUrl() {
        // A second Spring context with another property value cannot start a second Wicket
        // application in the same JVM, so the configured value is replaced for this test only.
        String standaardUrl = application.getMijnLoketUrl();
        ReflectionTestUtils.setField(application, "mijnLoketUrl", "https://loket.amsterdam.nl/jouwloket");
        try {
            tester.startPage(MarriageIntakePage.class);

            tester.assertContains("href=\"https://loket.amsterdam.nl/jouwloket\">Mijn Loket</a>");
        } finally {
            ReflectionTestUtils.setField(application, "mijnLoketUrl", standaardUrl);
        }
    }
}
