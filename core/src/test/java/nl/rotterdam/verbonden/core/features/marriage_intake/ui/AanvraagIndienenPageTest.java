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

    private static final String PAGE_BODY = "pageLayout:pageLayout_body:pageBody:pageBody_body";
    private static final String BEVESTIG_FORM = PAGE_BODY + ":bevestigForm";
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

        tester.startPage(ControlerenPage.class, makeDossierPageParameters(dossierId));

        tester.assertDisabled(BEVESTIG_BUTTON);
        tester.assertContains("Dit moet nog gebeuren voordat jullie je keuzes kunnen bevestigen");
    }

    @Test
    @WithMockUser(username = "999990020")
    void controlerenToontCorrecteDagnaamEnBevestigingsinformatieVoorDeKnop() {
        UUID dossierId = testData.maakCompleetDossier();

        tester.startPage(ControlerenPage.class, makeDossierPageParameters(dossierId));

        String html = tester.getLastResponseAsString();
        assertThat(html)
                .contains("Dé dag")
                .contains("Jullie keuze blijft 3 dagen gereserveerd.")
                .contains("Na bevestiging kunnen jullie de keuzes niet meer wijzigen.")
                .contains("Rotterdam stuurt je een factuur voor de kosten.")
                .doesNotContain("intake.sidebar.reservering.info");
        String confirmButtonMarkupId = tester.getComponentFromLastRenderedPage(BEVESTIG_BUTTON).getMarkupId();
        assertThat(html.indexOf("Jullie keuze blijft 3 dagen gereserveerd."))
                .isLessThan(html.indexOf("id=\"" + confirmButtonMarkupId + "\""));
    }

    @Test
    @WithMockUser(username = "999990020")
    void compleetDossier_bevestigButtonOpentBevestigingsdialoog() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(ControlerenPage.class, makeDossierPageParameters(dossierId));
        tester.assertEnabled(BEVESTIG_BUTTON);
        tester.assertInvisible(DIALOG_CONTENT);

        tester.executeAjaxEvent(BEVESTIG_BUTTON, "click");

        tester.assertVisible(DIALOG_CONTENT);
        tester.assertComponentOnAjaxResponse(DIALOG_CONTENT);
        tester.assertContains("Bevestig jullie keuzes");
        assertThat(tester.getLastResponseAsString()).doesNotContain("Rotterdam stuurt je een factuur");
        assertThat(tester.getLastResponseAsString()).contains(".showModal()");
    }

    @Test
    @WithMockUser(username = "999990020")
    void annuleren_sluitDialoogEnLaatDossierOngewijzigd() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(ControlerenPage.class, makeDossierPageParameters(dossierId));
        tester.executeAjaxEvent(BEVESTIG_BUTTON, "click");

        tester.executeAjaxEvent(BEVESTIG_FORM + ":" + DIALOG_ACTIES + "annulerenButton", "click");

        tester.assertInvisible(DIALOG_CONTENT);
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.CONCEPT);
    }

    @Test
    @WithMockUser(username = "999990020")
    void bevestigen_dientDossierInEnToontStatuspagina() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(ControlerenPage.class, makeDossierPageParameters(dossierId));
        tester.executeAjaxEvent(BEVESTIG_BUTTON, "click");

        // Zoals een browser: anders weigert Wicket's CSRF-bescherming de POST.
        tester.getRequest().setHeader("Origin", "http://localhost");
        tester.newFormTester(BEVESTIG_FORM).submit(DIALOG_ACTIES + "indienenButton");

        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.INGEDIEND);
        tester.assertRenderedPage(AanvraagStatusPage.class);
        tester.assertContains("De huwelijksaanvraag is ontvangen en wordt in behandeling genomen.");
        tester.assertContains("Wat gebeurt er nu\\?");
        assertThat(tester.getLastResponseAsString()).doesNotContain("bevestigButton");
    }

    @Test
    @WithMockUser(username = "999990020")
    void extraStapNavigeertNaarControlerenEnTerug() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(ExtrasPage.class, makeDossierPageParameters(dossierId));
        tester.assertEnabled(PAGE_BODY + ":volgendeStapContainer:volgendeStap");

        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.clickLink(PAGE_BODY + ":volgendeStapContainer:volgendeStap");

        tester.assertRenderedPage(ControlerenPage.class);
        tester.assertContains("Controleren");
        tester.assertContains("Samenvatting");
        tester.assertContains("Jullie gegevens");
        tester.assertContains("De getuigen");
        tester.assertContains("Totaal bedrag");
        tester.assertEnabled(PAGE_BODY + ":bevestigForm:bevestigButton");

        tester.assertEnabled(PAGE_BODY + ":vorigeStapContainer:vorigeStap");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.clickLink(PAGE_BODY + ":vorigeStapContainer:vorigeStap");

        tester.assertRenderedPage(ExtrasPage.class);
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
