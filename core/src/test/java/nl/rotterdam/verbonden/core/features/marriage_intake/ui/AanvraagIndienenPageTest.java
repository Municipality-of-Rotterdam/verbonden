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
                .contains("Controleer jullie keuzes goed.")
                .contains("Na bevestiging gaan jullie verder naar de betalingspagina.")
                .contains("Na betaling ontvangen jullie een bevestiging per e-mail.")
                .contains("Jullie reservering blijft 3 dagen geldig.")
                .contains("Binnen die periode kunnen jullie de aanvraag gratis aanpassen.")
                .contains("Daarna is wijzigen niet meer mogelijk.")
                .contains("Dan betalen jullie € 50 administratiekosten.")
                .contains("rd-status-icon rd-status-icon--current-check")
                .doesNotContain("<dialog");
        String confirmButtonMarkupId = tester.getComponentFromLastRenderedPage(BEVESTIG_BUTTON).getMarkupId();
        assertThat(html.indexOf("Controleer jullie keuzes goed."))
                .isLessThan(html.indexOf("id=\"" + confirmButtonMarkupId + "\""));
    }

    @Test
    @WithMockUser(username = "999990020")
    void statusWordtOnderControlerenInDeSidebarGetoond() {
        UUID dossierId = testData.maakCompleetDossier();
        marriageIntakeService.dienIn(dossierId);

        tester.startPage(AanvraagStatusPage.class, makeDossierPageParameters(dossierId));

        String html = tester.getLastResponseAsString();
        int controlerenSection = html.indexOf("rd-keuzes-section rd-progress-review");
        assertThat(html.indexOf("Controleren", controlerenSection))
                .isLessThan(html.indexOf("rd-keuzes-status", controlerenSection));
        tester.assertVisible(PAGE_BODY + ":keuzesSidebar:controlerenSection:statusContainer");
    }

    @Test
    @WithMockUser(username = "999990020")
    void bevestigenOpentBetalingspaginaZonderDossierInTeDienen() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(ControlerenPage.class, makeDossierPageParameters(dossierId));
        tester.assertEnabled(BEVESTIG_BUTTON);

        tester.getRequest().setHeader("Origin", "http://localhost");
        tester.newFormTester(BEVESTIG_FORM).submit("bevestigButton");

        tester.assertRenderedPage(BetalingPage.class);
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.CONCEPT);
    }

    @Test
    @WithMockUser(username = "999990020")
    void betalingUitvoerenDientDossierInEnToontStatuspagina() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(BetalingPage.class, makeDossierPageParameters(dossierId));
        String betalingForm = PAGE_BODY + ":betalingForm";
        tester.assertEnabled(betalingForm + ":betalingButton");

        // Zoals een browser: anders weigert Wicket's CSRF-bescherming de POST.
        tester.getRequest().setHeader("Origin", "http://localhost");
        tester.newFormTester(betalingForm).submit("betalingButton");

        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.INGEDIEND);
        tester.assertRenderedPage(AanvraagStatusPage.class);
        tester.assertContains("De huwelijksaanvraag is ontvangen en wordt in behandeling genomen.");
        tester.assertContains("Wat gebeurt er nu\\?");
        assertThat(tester.getLastResponseAsString()).doesNotContain("bevestigButton");
    }

    @Test
    @WithMockUser(username = "999990020")
    void betalingNietGeluktKeertTerugNaarControlerenMetMelding() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(BetalingPage.class, makeDossierPageParameters(dossierId));

        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.clickLink(PAGE_BODY + ":betalingMislukt");

        tester.assertRenderedPage(ControlerenPage.class);
        tester.assertVisible(PAGE_BODY + ":betalingMislukt");
        tester.assertContains("De betaling is niet gelukt of afgebroken.");
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.CONCEPT);
    }

    @Test
    @WithMockUser(username = "999990020")
    void betaalknopBlijftActiefAlsDossierNietCompleetIs() {
        UUID dossierId = testData.maakConceptDossier();
        tester.startPage(BetalingPage.class, makeDossierPageParameters(dossierId));
        String betalingForm = PAGE_BODY + ":betalingForm";

        tester.assertEnabled(betalingForm + ":betalingButton");
        tester.getRequest().setHeader("Origin", "http://localhost");
        tester.newFormTester(betalingForm).submit("betalingButton");

        tester.assertRenderedPage(ControlerenPage.class);
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.CONCEPT);
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
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.clickLink(PAGE_BODY + ":vorigeStapContainer:vorigeStap");
        tester.assertRenderedPage(DeGetuigenPage.class);
        String extrasIconMarkupId =
                tester.getComponentFromLastRenderedPage(PAGE_BODY + ":keuzesSidebar:extrasProgressIcon").getMarkupId();
        int extrasIconPosition = tester.getLastResponseAsString().indexOf("id=\"" + extrasIconMarkupId + "\"");
        assertThat(tester.getLastResponseAsString().substring(extrasIconPosition, extrasIconPosition + 120))
                .contains("rd-status-icon--complete");
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
