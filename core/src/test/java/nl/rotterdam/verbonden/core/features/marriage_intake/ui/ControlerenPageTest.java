package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import org.apache.wicket.util.tester.TagTester;
import org.junit.jupiter.api.Test;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ControlerenPageTest extends BaseWicketTest {

    private static final String PAGE_BODY = "pageLayout:pageLayout_body:pageBody:pageBody_body";

    @Autowired
    private CompleetDossierTestData testData;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Test
    @WithMockUser(username = "999990020")
    void stapnavigatieVolgtDeVolgordeVanDeStappen() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(JullieGegevensPage.class, makeDossierPageParameters(dossierId));

        click("volgendeStapContainer:volgendeStap");
        tester.assertRenderedPage(DeGetuigenPage.class);

        click("vorigeStapContainer:vorigeStap");
        tester.assertRenderedPage(JullieGegevensPage.class);

        tester.startPage(ControlerenPage.class, makeDossierPageParameters(dossierId));
        tester.assertInvisible(PAGE_BODY + ":volgendeStapContainer");
        click("vorigeStapContainer:vorigeStap");
        tester.assertRenderedPage(ExtrasPage.class);
    }

    @Test
    @WithMockUser(username = "999990020")
    void sidebarToontAfgerondeOnvoltooideEnActieveStapstatussen() {
        UUID dossierId = testData.maakConceptDossier();
        tester.startPage(DeGetuigenPage.class, makeDossierPageParameters(dossierId));

        assertThat(statusIcoon(IntakeStep.MIJN_DAG).getAttribute("class")).contains("rd-status-icon--complete");
        assertThat(statusIcoon(IntakeStep.DE_DAG).getAttribute("class")).contains("rd-status-icon--upcoming");
        assertThat(statusIcoon(IntakeStep.DE_GETUIGEN).getAttribute("class")).contains("rd-status-icon--current");
        assertThat(statusIcoon(IntakeStep.DE_GETUIGEN).getValue()).isEqualTo("4");
        assertThat(statusIcoon(IntakeStep.EXTRAS).getAttribute("class")).contains("rd-status-icon--upcoming");
    }

    @Test
    @WithMockUser(username = "999990020")
    void extrasBlijvenAfgerondNaDoorlopenOokInEenNieuweSessie() {
        UUID dossierId = testData.maakConceptDossier();
        tester.startPage(ExtrasPage.class, makeDossierPageParameters(dossierId));

        // Terug naar de vorige stap telt niet als doorlopen
        click("vorigeStapContainer:vorigeStap");
        tester.startPage(DeDagPage.class, makeDossierPageParameters(dossierId));
        assertThat(statusIcoon(IntakeStep.EXTRAS).getAttribute("class")).contains("rd-status-icon--upcoming");

        marriageIntakeService.slaAantalBezoekersOp(dossierId, 4);
        tester.startPage(ExtrasPage.class, makeDossierPageParameters(dossierId));
        click("volgendeStapContainer:volgendeStap");
        tester.assertRenderedPage(ControlerenPage.class);

        // Een nieuwe request zonder extra URL-parameters: de voortgang komt uit het dossier
        tester.startPage(DeDagPage.class, makeDossierPageParameters(dossierId));
        assertThat(statusIcoon(IntakeStep.EXTRAS).getAttribute("class")).contains("rd-status-icon--complete");
    }

    @Test
    @WithMockUser(username = "999990020")
    void samenvattingToontTaalEnAantalBezoekers() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(ControlerenPage.class, makeDossierPageParameters(dossierId));

        assertThat(tester.getLastResponseAsString())
                .contains("Taal van de ceremonie")
                .contains("Nederlands")
                .contains("Aantal bezoekers");
    }

    @Test
    @WithMockUser(username = "999990020")
    void totaalStaatInDeSamenvattingEnBevestigenDaarbuiten() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(ControlerenPage.class, makeDossierPageParameters(dossierId));

        String html = tester.getLastResponseAsString();
        assertThat(html).contains("rd-intake-content--unbordered");
        String samenvatting = TagTester.createTagByAttribute(html, "class", "rd-controleer-kaart").getValue();
        assertThat(samenvatting).contains("rd-controleer-totaal").doesNotContain("rd-controleer-bevestigen");
        assertThat(TagTester.createTagByAttribute(html, "class", "rd-controleer-bevestigen")).isNotNull();
    }

    @Test
    void naamkeuzeVoegtEigenAchternaamNietDubbelToe() {
        PartnerGegevensDto partner1 = partner("Rosa", "van Rozen", "van Rozen - van Mooije");
        PartnerGegevensDto partner2 = partner("Sabina Ellen", "van Mooije", "van Mooije - van Rozen");

        assertThat(ControlerenPage.naamkeuze(partner1)).isEqualTo("Rosa van Rozen - van Mooije");
        assertThat(ControlerenPage.naamkeuze(partner2)).isEqualTo("Sabina Ellen van Mooije - van Rozen");
    }

    @Test
    void naamkeuzeMetAlleenEigenAchternaamToontGeenDubbeleAchternaam() {
        assertThat(ControlerenPage.naamkeuze(partner("Rosa", "van Rozen", "van Rozen")))
                .isEqualTo("Rosa van Rozen");
    }

    private static PartnerGegevensDto partner(String voornamen, String achternaam, String gekozenAchternaam) {
        return new PartnerGegevensDto(1, null, null, achternaam, voornamen, null,
                null, null, null, null, null, gekozenAchternaam, 0);
    }

    private void click(String linkId) {
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.clickLink(PAGE_BODY + ":" + linkId);
    }

    private TagTester statusIcoon(IntakeStep step) {
        return SidebarStatusIconen.statusIcoon(tester.getLastResponseAsString(), step);
    }
}
