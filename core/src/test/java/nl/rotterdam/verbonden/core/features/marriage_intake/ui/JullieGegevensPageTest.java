package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveGetuigenDto;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JullieGegevensPageTest extends BaseWicketTest {

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    private static final String SIDEBAR = "pageLayout:pageLayout_body:pageBody:pageBody_body:keuzesSidebar:";

    private static final String PARTNER_CARDS = "pageLayout:pageLayout_body:pageBody:pageBody_body:partnerCards:";

    private UUID createdDossierId;

    @AfterEach
    void cleanup() {
        if (createdDossierId != null) {
            marriageIntakeService.slaGetuigenOp(createdDossierId, List.of());
            marriageIntakeService.delete(createdDossierId);
            createdDossierId = null;
        }
    }

    @Test
    @WithMockUser(username = "999990007")
    void testRender() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990007")));

        PageParameters params = new PageParameters();
        params.add("dossierId", createdDossierId.toString());
        tester.startPage(JullieGegevensPage.class, params);
        tester.assertRenderedPage(JullieGegevensPage.class);
    }

    @Test
    @WithMockUser(username = "999990007")
    void toontKopieerbareUitnodigingslinkZolangPartnerNietIsGekoppeld() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990007")));

        PageParameters params = new PageParameters();
        params.add("dossierId", createdDossierId.toString());
        tester.startPage(JullieGegevensPage.class, params);

        String html = tester.getLastResponseAsString();
        assertThat(html).containsPattern("id=\"partner-uitnodiging-url\"[^>]*value=\"[^\"]*/mijn-dag/" + createdDossierId + "\"");
        assertThat(html).contains("data-kopieer-doel=\"partner-uitnodiging-url\"");
        assertThat(html).contains("link-kopieren");

        tester.startResourceReference(new PackageResourceReference(JullieGegevensPage.class, "link-kopieren.js"));
        assertThat(tester.getLastResponseAsString()).contains("navigator.clipboard");
    }

    @Test
    @WithMockUser(username = "999990007")
    void sidebarLinkNaarJullieGegevensWerktVanafGetuigenPagina() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990007")));

        PageParameters params = new PageParameters();
        params.add("dossierId", createdDossierId.toString());
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.startPage(DeGetuigenPage.class, params);

        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.clickLink(SIDEBAR + "gegevensLink");

        tester.assertRenderedPage(JullieGegevensPage.class);
    }

    @Test
    @WithMockUser(username = "999990007")
    void sidebarLinkAlgemeenNavigeertNaarMijnDag() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null,
                        new BurgerServiceNummer("999990007")));
        PageParameters params = new PageParameters().add("dossierId", createdDossierId.toString());
        tester.startPage(DeGetuigenPage.class, params);

        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.clickLink(SIDEBAR + "dedagLink");

        tester.assertRenderedPage(MarriageIntakePage.class);
    }

    @Test
    @WithMockUser(username = "999990007")
    void sidebarLinkDatumEnTijdNavigeertNaarDeDag() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null,
                        new BurgerServiceNummer("999990007")));
        PageParameters params = new PageParameters().add("dossierId", createdDossierId.toString());
        tester.startPage(DeGetuigenPage.class, params);

        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.clickLink(SIDEBAR + "dossierGegevens:datumLink");

        tester.assertRenderedPage(DeDagPage.class);
    }

    @Test
    @WithMockUser(username = "999990007")
    void zijbalkHerinnertAanPartnerZonderLink() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990007")));

        PageParameters params = new PageParameters();
        params.add("dossierId", createdDossierId.toString());
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.startPage(DeGetuigenPage.class, params);
        tester.assertVisible(SIDEBAR + "partnerHerinnering");
        String html = tester.getLastResponseAsString();
        assertThat(html).contains("Uw partner heeft het dossier nog niet gekoppeld.")
                .doesNotContain("partnerUitnodigenLink");
        int sidebarStart = html.indexOf("rd-keuzes-panel");
        assertThat(html.indexOf("Uw partner heeft het dossier nog niet gekoppeld.", sidebarStart))
                .isLessThan(html.indexOf("De getuigen", sidebarStart));
    }

    @Test
    @WithMockUser(username = "999990007")
    void sidebarToontStatusPerStapMetAndereMarkeringVoorHuidigeStap() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990007")));

        PageParameters params = new PageParameters();
        params.add("dossierId", createdDossierId.toString());
        tester.startPage(DeGetuigenPage.class, params);

        String html = tester.getLastResponseAsString();
        assertThat(html)
                .contains("rd-status-icon--complete")
                .contains("rd-status-icon--upcoming\">2</span>")
                .contains("rd-status-icon--upcoming\">3</span>")
                .contains("rd-status-icon--current\">4</span>")
                .contains("rd-status-icon--upcoming\">5</span>")
                .contains("rd-status-icon--upcoming\">6</span>");
    }

    @Test
    @WithMockUser(username = "999990007")
    void sidebarToontLegeTekstenTotNamenZijnIngevuld() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990007")));

        PageParameters params = new PageParameters();
        params.add("dossierId", createdDossierId.toString());
        tester.startPage(DeGetuigenPage.class, params);

        tester.assertVisible(SIDEBAR + "geenGegevens");
        tester.assertVisible(SIDEBAR + "geenGetuigen");
        assertThat(tester.getLastResponseAsString())
                .contains("Nog geen gegevens ingevuld")
                .contains("Nog geen getuigen ingevuld");
    }

    @Test
    @WithMockUser(username = "999990093")
    void sidebarToontGekozenAchternaamEnGetuigenAlsGewoneTekst() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990093")));
        marriageIntakeService.slaPartnerGegevensOp(createdDossierId, 1, "Buitelaar");
        marriageIntakeService.slaGetuigeOp(createdDossierId, new SaveGetuigenDto(1, "Anna van Bergen"));

        PageParameters params = new PageParameters();
        params.add("dossierId", createdDossierId.toString());
        tester.startPage(DeGetuigenPage.class, params);

        tester.assertInvisible(SIDEBAR + "geenGegevens");
        tester.assertInvisible(SIDEBAR + "geenGetuigen");
        String html = tester.getLastResponseAsString();
        assertThat(html)
                .contains("Buitelaar")
                .contains("Anna van Bergen");
        assertNotInsideLink(html, "Buitelaar");
        assertNotInsideLink(html, "Anna van Bergen");
    }

    private void assertNotInsideLink(String html, String text) {
        int textIndex = html.indexOf(text);
        assertThat(textIndex).isGreaterThanOrEqualTo(0);
        assertThat(html.lastIndexOf("</a>", textIndex))
                .isGreaterThan(html.lastIndexOf("<a", textIndex));
    }

    @Test
    @WithMockUser(username = "999990007")
    void zijbalkToontGeenHerinneringAlsPartnerGekoppeldIs() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990007")));
        marriageIntakeService.acceptInvitation(createdDossierId, new BurgerServiceNummer("999990019"));

        PageParameters params = new PageParameters();
        params.add("dossierId", createdDossierId.toString());
        tester.startPage(DeGetuigenPage.class, params);

        tester.assertInvisible(SIDEBAR + "partnerHerinnering");
    }

    @Test
    @WithMockUser(username = "999990019")
    void partnerKanAchternaamVanDeAnderKiezen() {
        BurgerServiceNummer partner1 = new BurgerServiceNummer("999990007");
        BurgerServiceNummer partner2 = new BurgerServiceNummer("999990019");
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, partner1));
        marriageIntakeService.acceptInvitation(createdDossierId, partner2);

        PageParameters params = new PageParameters();
        params.add("dossierId", createdDossierId.toString());
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.startPage(JullieGegevensPage.class, params);
        tester.assertVisible(PARTNER_CARDS + "0:kiesAchternaamSection");
        tester.assertVisible(PARTNER_CARDS + "1:kiesAchternaamSection");

        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.newFormTester(PARTNER_CARDS + "0:naamKiezenDialog:naamKiezenForm").submit();

        assertThat(marriageIntakeService.findPartnerGegevens(createdDossierId))
                .filteredOn(p -> p.bsn().equals(partner1))
                .singleElement()
                .satisfies(p -> assertThat(p.gekozenAchternaam()).isNotNull());
        assertThat(marriageIntakeService.findPartnerGegevens(createdDossierId))
                .filteredOn(p -> p.bsn().equals(partner2))
                .singleElement()
                .satisfies(p -> assertThat(p.gekozenAchternaam()).isNull());
    }
}
