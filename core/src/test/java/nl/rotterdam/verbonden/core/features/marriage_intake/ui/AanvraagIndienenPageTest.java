package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.dossier_administration.application.DossierAdministrationService;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class AanvraagIndienenPageTest extends BaseWicketTest {

    private static final String SIDEBAR = "pageLayout:pageLayout_body:pageBody:pageBody_body:keuzesSidebar";
    private static final String BEVESTIG_FORM = SIDEBAR + ":bevestigForm";
    private static final String BEVESTIG_BUTTON = BEVESTIG_FORM + ":bevestigButton";
    private static final String DIALOG_CONTENT = BEVESTIG_FORM + ":bevestigDialog:content";
    private static final String DIALOG_ACTIES = "bevestigDialog:content:footerContent:acties:acties_body:";

    @Autowired
    private CompleetDossierTestData testData;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @Test
    @WithMockUser(username = "999990020")
    void onvolledigDossier_bevestigButtonIsUitgeschakeld() {
        UUID dossierId = testData.maakConceptDossier();

        tester.startPage(DeDagPage.class, makeDossierPageParameters(dossierId));

        tester.assertDisabled(BEVESTIG_BUTTON);
        tester.assertContains("Vul eerst alle onderdelen in");
    }

    @Test
    @WithMockUser(username = "999990020")
    void compleetDossier_bevestigButtonOpentBevestigingsdialoog() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(DeDagPage.class, makeDossierPageParameters(dossierId));
        tester.assertEnabled(BEVESTIG_BUTTON);
        tester.assertInvisible(DIALOG_CONTENT);

        tester.executeAjaxEvent(BEVESTIG_BUTTON, "click");

        tester.assertVisible(DIALOG_CONTENT);
        tester.assertComponentOnAjaxResponse(DIALOG_CONTENT);
        tester.assertContains("Wil je je keuzes definitief vastleggen\\?");
        tester.assertContains("Rotterdam stuurt je een factuur voor de kosten.");
        assertThat(tester.getLastResponseAsString()).contains(".showModal()");
    }

    @Test
    @WithMockUser(username = "999990020")
    void annuleren_sluitDialoogEnLaatDossierOngewijzigd() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(DeDagPage.class, makeDossierPageParameters(dossierId));
        tester.executeAjaxEvent(BEVESTIG_BUTTON, "click");

        tester.executeAjaxEvent(BEVESTIG_FORM + ":" + DIALOG_ACTIES + "annulerenButton", "click");

        tester.assertInvisible(DIALOG_CONTENT);
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.CONCEPT);
    }

    @Test
    @WithMockUser(username = "999990020")
    void bevestigen_dientDossierInEnToontStatuspagina() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(DeDagPage.class, makeDossierPageParameters(dossierId));
        tester.executeAjaxEvent(BEVESTIG_BUTTON, "click");

        // Zoals een browser: anders weigert Wicket's CSRF-bescherming de POST.
        tester.getRequest().setHeader("Origin", "http://localhost");
        tester.newFormTester(BEVESTIG_FORM).submit(DIALOG_ACTIES + "indienenButton");

        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.INGEDIEND);
        tester.assertRenderedPage(AanvraagStatusPage.class);
        tester.assertContains("De huwelijksaanvraag is ontvangen en wordt in behandeling genomen.");
        tester.assertContains("Wat gebeurt er nu\\?");
        tester.assertInvisible(BEVESTIG_FORM);
    }

    @Test
    @WithMockUser(username = "999990020")
    void ingediendDossier_wijzigpaginaStuurtDoorNaarStatuspagina() {
        UUID dossierId = testData.maakCompleetDossier();
        marriageIntakeService.dienIn(dossierId);

        tester.startPage(DeGetuigenPage.class, makeDossierPageParameters(dossierId));
        tester.assertRenderedPage(AanvraagStatusPage.class);

        tester.startPage(DatumKiezenPage.class, makeDossierPageParameters(dossierId));
        tester.assertRenderedPage(AanvraagStatusPage.class);

        tester.startPage(MarriageIntakePage.class);
        tester.assertRenderedPage(AanvraagStatusPage.class);
    }

    @Test
    @WithMockUser(username = "999990020")
    void conceptDossier_statuspaginaStuurtDoorNaarDeDag() {
        UUID dossierId = testData.maakCompleetDossier();

        tester.startPage(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));

        tester.assertRenderedPage(DeDagPage.class);
    }

    @Test
    @WithMockUser(username = "999990020")
    void afgewezenDossier_statuspaginaToontAfwijzing() {
        UUID dossierId = testData.maakCompleetDossier();
        marriageIntakeService.dienIn(dossierId);
        dossierAdministrationService.wijsAf(dossierId);

        tester.startPage(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));

        tester.assertRenderedPage(AanvraagStatusPage.class);
        tester.assertContains("Jullie aanvraag is afgewezen");
        assertThat(tester.getLastResponseAsString()).doesNotContain("Wat gebeurt er nu?");
    }
}
