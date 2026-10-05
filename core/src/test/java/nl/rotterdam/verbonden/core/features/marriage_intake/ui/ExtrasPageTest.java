package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.application.TrouwboekjeAdministrationService;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.CreateTrouwboekjeDto;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.visit.IVisit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ExtrasPageTest extends BaseWicketTest {

    private static final String INTERNATIONALE_AKTE_INTRO =
            "Gaan jullie de huwelijksakte in het buitenland gebruiken? Kies dan voor een internationaal uittreksel. Dat is een samenvatting van jullie huwelijksakte met uitleg in 10 talen: Nederlands, Engels, Frans, Duits, Spaans, Grieks, Italiaans, Portugees, Turks en Servo-Kroatisch.";

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private TrouwboekjeAdministrationService trouwboekjeAdministrationService;

    @Test
    @WithMockUser(username = "999990007")
    void trouwboekjeKiezen_slaatOpEnVerverstAlleenKeuzesEnSidebar() {
        long trouwboekjeId = trouwboekjeAdministrationService.create(new CreateTrouwboekjeDto(
                "Linnen trouwboekje", "Linnen", "/trouwboekjes/linnen.webp", new BigDecimal("42.50"), null, null));
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, null, new BurgerServiceNummer("999990007")));
        ExtrasPage page = tester.startPage(ExtrasPage.class, new PageParameters().add("dossierId", dossierId.toString()));

        CheckBox keuze = page.visitChildren(CheckBox.class, (CheckBox checkBox, IVisit<CheckBox> visit) -> {
            if (checkBox.getMarkupId().equals("trouwboekje-" + trouwboekjeId)) {
                visit.stop(checkBox);
            }
        });
        String formPad = "pageLayout:pageLayout_body:pageBody:pageBody_body:extrasForm";
        Component trouwboekjeSection = page.get(formPad + ":trouwboekjeSection");
        FormTester formTester = tester.newFormTester(formPad, false);
        formTester.setValue(keuze, "true");
        tester.executeAjaxEvent(keuze, "change");

        // Geen volledige page-redirect (die liet de pagina naar boven springen), maar een gedeeltelijke ajax-update
        tester.assertRenderedPage(ExtrasPage.class);
        tester.assertComponentOnAjaxResponse(page.keuzesSidebar);
        tester.assertComponentOnAjaxResponse(trouwboekjeSection);
        assertThat(tester.getLastResponseAsString()).contains("Linnen trouwboekje");
        assertThat(marriageIntakeService.findExtrasSelecties(dossierId).trouwboekjeId()).isEqualTo(trouwboekjeId);
    }

    @Test
    @WithMockUser(username = "999990007")
    void kleinHuwelijk_toontInternationaleAkteOptie() {
        assertInternationaleAkteZichtbaarVoor(CeremonieSoort.KLEIN, new BurgerServiceNummer("999990007"));
    }

    @Test
    @WithMockUser(username = "999990019")
    void middelgrootHuwelijk_toontInternationaleAkteOptie() {
        assertInternationaleAkteZichtbaarVoor(CeremonieSoort.MIDDELGROOT, new BurgerServiceNummer("999990019"));
    }

    @Test
    @WithMockUser(username = "999990020")
    void grootHuwelijk_toontInternationaleAkteOptie() {
        assertInternationaleAkteZichtbaarVoor(CeremonieSoort.GROOT, new BurgerServiceNummer("999990020"));
    }

    @Test
    @WithMockUser(username = "999990202")
    void geregistreerdPartnerschap_toontGeenInternationaleAkteOptie() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.GEREGISTREERD_PARTNERSCHAP, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990202")));

        tester.startPage(ExtrasPage.class, new PageParameters().add("dossierId", dossierId.toString()));

        tester.assertRenderedPage(ExtrasPage.class);
        assertThat(tester.getLastResponseAsString()).doesNotContain(INTERNATIONALE_AKTE_INTRO);
    }

    private void assertInternationaleAkteZichtbaarVoor(CeremonieSoort ceremonieSoort, BurgerServiceNummer bsn) {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, ceremonieSoort, null, bsn));

        tester.startPage(ExtrasPage.class, new PageParameters().add("dossierId", dossierId.toString()));

        tester.assertRenderedPage(ExtrasPage.class);
        assertThat(tester.getLastResponseAsString()).contains(INTERNATIONALE_AKTE_INTRO);
        assertThat(tester.getLastResponseAsString()).contains("Internationale huwelijksakte");
    }
}
