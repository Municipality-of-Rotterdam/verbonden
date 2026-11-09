package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.config.PlanningConfig;
import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.location_administration.application.LocationAdministrationService;
import nl.rotterdam.verbonden.core.features.location_administration.domain.CreateBeschikbaarheidDto;
import nl.rotterdam.verbonden.core.features.location_administration.domain.CreateLocatieDto;
import nl.rotterdam.verbonden.core.features.location_administration.domain.HuwelijksType;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class DeDagPageTest extends BaseWicketTest {

    private static final String PAGE_BODY = "pageLayout:pageLayout_body:pageBody:pageBody_body:";

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private LocationAdministrationService locationAdministrationService;

    @Autowired
    private PlanningConfig planningConfig;

    @Autowired
    private Clock clock;

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void meldingIsVerborgenTotEenKaartWordtAangeklikt() {
        startPage(maakDossier(CeremonieSoort.KLEIN));

        tester.assertInvisible(PAGE_BODY + "nietBeschikbaarAlert");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void kleineCeremonie_klikOpLocatie_legtUitWaaromNietBeschikbaar() {
        startPage(maakDossier(CeremonieSoort.KLEIN));

        klikOp("locatieLink");

        tester.assertVisible(PAGE_BODY + "nietBeschikbaarAlert");
        tester.assertContains("Bij een kleine of middelgrote ceremonie kunt u geen trouwlocatie kiezen");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void kleineCeremonie_klikOpTrouwambtenaar_legtUitWaaromNietBeschikbaar() {
        startPage(maakDossier(CeremonieSoort.KLEIN));

        klikOp("babsLink");

        tester.assertContains("Bij een kleine of middelgrote ceremonie kunt u geen voorkeur voor een trouwambtenaar opgeven");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void groteCeremonie_klikOpLocatie_opentKeuzePagina() {
        startPage(maakDossier(CeremonieSoort.GROOT));

        klikOp("locatieLink");

        tester.assertRenderedPage(LocatieKiezenPage.class);
        tester.assertContains("Kies uw eigen locatie");
        tester.assertContains("Naam van de locatie");
        tester.assertContains("Adres van de locatie");
        tester.assertContains("Telefoonnummer van de locatie");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void locatiekaartToontVeiligeHtmlAfbeeldingEnInformatielink() {
        long locatieId = locationAdministrationService.create(new CreateLocatieDto(
                "Tuinzaal",
                "/assets/tuinzaal.jpg",
                "<p>Een <strong>mooie</strong> plek</p><script>alert('onveilig')</script>",
                "https://example.test/tuinzaal"));
        LocalDate datum = LocalDate.now(clock).plusDays(planningConfig.getVanafDagen());
        locationAdministrationService.createBeschikbaarheid(new CreateBeschikbaarheidDto(
                locatieId, HuwelijksType.REGULIER, datum.getDayOfWeek(), LocalTime.of(9, 0), LocalTime.of(9, 10),
                10, Bedrag.ZERO, datum, datum));
        startPage(maakDossier(CeremonieSoort.GROOT));

        klikOp("locatieLink");

        String html = tester.getLastResponseAsString();
        assertThat(html)
                .contains("<img")
                .contains("src=\"/assets/tuinzaal.jpg\"")
                .contains("<strong>mooie</strong>")
                .contains("href=\"https://example.test/tuinzaal\"")
                .contains("Meer informatie over deze locatie")
                .doesNotContain("<script>")
                .doesNotContain("alert('onveilig')");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void vrijeLocatie_verplichteVeldenZijnNodigVoorOpslaan() {
        UUID dossierId = maakDossier(CeremonieSoort.GROOT);
        startPage(dossierId);
        klikOp("locatieLink");

        FormTester form = formTester("vrijeTrouwlocatieForm");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("vrijeTrouwlocatieForm", "opslaan"));

        tester.assertRenderedPage(LocatieKiezenPage.class);
        assertThat(marriageIntakeService.findVrijeTrouwlocatie(dossierId)).isEmpty();

        form = formTester("vrijeTrouwlocatieForm");
        form.setValue(veld("vrijeTrouwlocatieForm", "naam"), "Tuinzaal");
        form.setValue(veld("vrijeTrouwlocatieForm", "adres"), "Straat 1, Rotterdam");
        form.setValue(veld("vrijeTrouwlocatieForm", "telefoonnummer"), "0101234567");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("vrijeTrouwlocatieForm", "opslaan"));

        tester.assertRenderedPage(DeDagPage.class);
        assertThat(marriageIntakeService.findByDossierId(dossierId).huwelijksLocatie()).isEqualTo("Tuinzaal");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void kleineCeremonie_introNoemtDatJullieAlleenDeDatumKiezen() {
        startPage(maakDossier(CeremonieSoort.KLEIN));

        tester.assertContains("Daarbij kiest u alleen de datum");
        tester.assertContainsNot("Begin bij het onderdeel dat u het belangrijkst vindt");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void groteCeremonie_introLaatJullieZelfBeginnenBijHetBelangrijksteOnderdeel() {
        startPage(maakDossier(CeremonieSoort.GROOT));

        tester.assertContains("Begin bij het onderdeel dat u het belangrijkst vindt");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void controlerenPaginaNoemtWatNogIngevuldMoetWorden_enLinktNaarDeJuisteStap() {
        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);
        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        zetNavigatieHeaders();
        tester.startPage(ControlerenPage.class, params);

        tester.assertContains("Kies een datum");
        tester.assertContains("Kies een trouwlocatie");
        tester.assertContains("Uw partner moet nog inloggen en meedoen");
        tester.assertContains("Kies allebei de achternaam die u wilt gebruiken");
        tester.assertContains("Vul de gegevens van alle getuigen in");

        klikOp("bevestigForm:nogNietCompleetHint:specifiek:onderdelen:4:link");

        tester.assertRenderedPage(DeGetuigenPage.class);
    }

    private void startPage(UUID dossierId) {
        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        zetNavigatieHeaders();
        tester.startPage(DeDagPage.class, params);
        tester.assertRenderedPage(DeDagPage.class);
    }

    private void klikOp(String linkId) {
        zetNavigatieHeaders();
        tester.clickLink(PAGE_BODY + linkId);
    }

    /** Same-origin navigation headers, so the click passes the CSRF check. */
    private void zetNavigatieHeaders() {
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
    }

    /** Dossier van de ingelogde burger (zie {@code @WithMockUser}), zoals de intake het aanmaakt. */
    private UUID maakDossier(CeremonieSoort soort) {
        BurgerServiceNummer ingelogd = new BurgerServiceNummer(
                SecurityContextHolder.getContext().getAuthentication().getName());
        return marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, soort, null, ingelogd));
    }
}
