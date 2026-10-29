package nl.rotterdam.verbonden.core.features.trouwlijst_administration.ui;

import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@WithMockUser(username = "planner", roles = Roles.PLANNER)
class TrouwlijstAdministrationPageTest extends BaseWicketTest {

    @Test
    void zonderWeekGeenExportknop() {
        tester.startPage(TrouwlijstAdministrationPage.class);

        tester.assertRenderedPage(TrouwlijstAdministrationPage.class);
        tester.assertInvisible(pad("exporteerLink"));
    }

    @Test
    void naWeekkeuzeToontDeExportknop() {
        tester.startPage(TrouwlijstAdministrationPage.class);
        String weekVeld = veld("filterForm", "input");

        formTester("filterForm").select(weekVeld, 1);
        tester.executeAjaxEvent(pad("filterForm") + ":" + weekVeld, "change");

        tester.assertVisible(pad("exporteerLink"));
        assertThat(tester.getLastResponseAsString()).contains("Export lijst");
    }
}
