package nl.rotterdam.verbonden.core.features.trouwlijst_administration.ui;

import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import nl.rotterdam.verbonden.core.integration_test.InstelbareKlok;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@WithMockUser(username = "planner", roles = Roles.PLANNER)
class TrouwlijstAdministrationPageTest extends BaseWicketTest {

    @Autowired
    private InstelbareKlok klok;

    @AfterEach
    void zetKlokTerug() {
        klok.zetTerug();
    }

    @Test
    void zonderWeekGeenExportknop() {
        tester.startPage(TrouwlijstAdministrationPage.class);

        tester.assertRenderedPage(TrouwlijstAdministrationPage.class);
        tester.assertInvisible(pad("exporteerForm"));
    }

    /**
     * Het ISO-weeknummer van de maandag: rond de jaarwisseling hoort 29-12-2025 bij week 1 van 2026, en 2020
     * heeft een week 53.
     */
    @ParameterizedTest
    @CsvSource({
            "2026-11-20, Week 46: 09-11-2026 t/m 15-11-2026",
            "2025-12-31, Week 52: 22-12-2025 t/m 28-12-2025",
            "2025-12-31, Week 1: 29-12-2025 t/m 04-01-2026",
            "2021-01-03, Week 53: 28-12-2020 t/m 03-01-2021",
            "2021-01-03, Week 1: 04-01-2021 t/m 10-01-2021"
    })
    void weekkeuzeToontHetWeeknummer(LocalDate vandaag, String verwachteWeek) {
        klok.zetOp(vandaag);

        tester.startPage(TrouwlijstAdministrationPage.class);

        assertThat(tester.getLastResponseAsString()).containsPattern("<option[^>]*>" + verwachteWeek + "</option>");
    }

    @Test
    void naWeekkeuzeToontDeExportknop() {
        tester.startPage(TrouwlijstAdministrationPage.class);
        String weekVeld = veld("filterForm", "input");

        formTester("filterForm").select(weekVeld, 1);
        tester.executeAjaxEvent(pad("filterForm") + ":" + weekVeld, "change");

        tester.assertVisible(pad("exporteerForm"));
        assertThat(tester.getLastResponseAsString()).contains("Export lijst")
                .contains("<button");
    }

    @Test
    void exportknopIsEenGestijldeKnop() {
        tester.startPage(TrouwlijstAdministrationPage.class);
        String weekVeld = veld("filterForm", "input");
        formTester("filterForm").select(weekVeld, 1);
        tester.executeAjaxEvent(pad("filterForm") + ":" + weekVeld, "change");
        assertThat(tester.getLastResponseAsString()).containsPattern("<button[^>]*utrecht-button[^>]*>\\s*(<[^>]*>)*\\s*Export lijst");
    }
}
