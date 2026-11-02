package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.features.location_administration.application.LocationAdministrationService;
import nl.rotterdam.verbonden.core.features.location_administration.domain.CreateBeschikbaarheidDto;
import nl.rotterdam.verbonden.core.features.location_administration.domain.HuwelijksType;
import nl.rotterdam.verbonden.core.features.location_administration.domain.ListLocatieDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.IntakeMarriageTypeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class DatumKiezenPageTest extends BaseWicketTest {

    private static final String VERDER_CONTAINER = "pageLayout:pageLayout_body:pageBody:pageBody_body:verderContainer";

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private LocationAdministrationService locationAdministrationService;

    @BeforeEach
    void setUpData() {
        for (ListLocatieDto locatie : locationAdministrationService.findAllLocaties()) {
            locationAdministrationService.findBeschikbaarheden(locatie.id())
                    .forEach(b -> locationAdministrationService.deleteBeschikbaarheid(b.id()));
            locationAdministrationService.findNietBeschikbareDagen(locatie.id())
                    .forEach(d -> locationAdministrationService.deleteNietBeschikbareDag(d.id()));
        }
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void paginaRendertMetDatePicker() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        tester.startPage(DatumKiezenPage.class, params);

        tester.assertRenderedPage(DatumKiezenPage.class);

        String response = tester.getLastResponseAsString();
        assertThat(response).contains("rods-date-picker");
        assertThat(response).contains("utrecht-button");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void datePickerBeschikbareSlots() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        tester.startPage(DatumKiezenPage.class, params);

        tester.assertRenderedPage(DatumKiezenPage.class);

        String response = tester.getLastResponseAsString();
        assertThat(response).contains("el.options");
    }

    @Test
    @WithMockUser(username = "999990019", roles = "BURGER")
    void datePickerGeenBeschikbareSlotsZonderBeschikbaarheid() {
        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        tester.startPage(DatumKiezenPage.class, params);

        tester.assertRenderedPage(DatumKiezenPage.class);

        String response = tester.getLastResponseAsString();
        assertThat(response).contains("rods-date-picker");
        assertThat(response).doesNotContain("el.options");
    }

    @Test
    @WithMockUser(username = "999990019", roles = "BURGER")
    void breadcrumbLinktNaarGemounteMijnDagEnDeDagPagina() {
        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        tester.startPage(DatumKiezenPage.class, dossierParams(dossierId));

        String response = tester.getLastResponseAsString();
        // DatumKiezenPage hangt onder /mijn-dag/{dossierId}/de-dag/datum-kiezen
        assertThat(response).contains("href=\"../../" + dossierId + "\"><span>Mijn dag</span>");
        assertThat(response).contains("href=\"../de-dag\"><span>Dé dag</span>");
        assertThat(response).doesNotContain("bookmarkable");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void eenvoudigDossierToontBeschikbareSlots() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.MIDDELGROOT);

        maakBeschikbaarheid(locatieId, HuwelijksType.EENVOUDIG, DayOfWeek.WEDNESDAY,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 15);

        UUID dossierId = maakDossier(CeremonieSoort.MIDDELGROOT);

        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        tester.startPage(DatumKiezenPage.class, params);

        tester.assertRenderedPage(DatumKiezenPage.class);

        String response = tester.getLastResponseAsString();
        assertThat(response).contains("el.options");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void volledigeDossierFlow_vanIntakeNaarDatumKiezen() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        tester.startPage(MarriageIntakePage.class);
        tester.assertRenderedPage(MarriageIntakePage.class);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        PageParameters deDagParams = new PageParameters();
        deDagParams.add("dossierId", dossierId.toString());
        tester.startPage(DeDagPage.class, deDagParams);
        tester.assertRenderedPage(DeDagPage.class);

        PageParameters datumParams = new PageParameters();
        datumParams.add("dossierId", dossierId.toString());
        tester.startPage(DatumKiezenPage.class, datumParams);
        tester.assertRenderedPage(DatumKiezenPage.class);

        String response = tester.getLastResponseAsString();
        assertThat(response).contains("rods-date-picker");
        assertThat(response).contains("el.options");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void geboektTijdslotIsGeselecteerdEnKiesbaar() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);
        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);
        LocalDateTime geboekt = marriageIntakeService.findAllBeschikbareSlots(dossierId).iterator().next();
        marriageIntakeService.slaAfspraakOp(dossierId, geboekt.toLocalDate(), geboekt.toLocalTime());

        tester.startPage(DatumKiezenPage.class, dossierParams(dossierId));

        tester.assertRenderedPage(DatumKiezenPage.class);
        String geboektAlsTekst = geboekt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        assertThat(tester.getLastResponseAsString())
                .contains("value=\"" + geboektAlsTekst + "\"")
                .contains("{ \"value\": \"" + geboektAlsTekst + "\" }");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void geboektTijdslotOpnieuwBevestigen_behoudtAfspraak() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);
        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);
        LocalDateTime geboekt = marriageIntakeService.findAllBeschikbareSlots(dossierId).iterator().next();
        marriageIntakeService.slaAfspraakOp(dossierId, geboekt.toLocalDate(), geboekt.toLocalTime());

        tester.startPage(DatumKiezenPage.class, dossierParams(dossierId));
        // Zoals een browser: anders weigert Wicket's CSRF-bescherming de POST.
        tester.getRequest().setHeader("Origin", "http://localhost");
        formTester("datumKiezenForm").submit();

        tester.assertRenderedPage(DeDagPage.class);
        assertThat(marriageIntakeService.findDatumTijdHuwelijk(dossierId)).isEqualTo(geboekt);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Hulpmethoden
    // ──────────────────────────────────────────────────────────────────────────

    private long vindGekoppeldeLocatieId(CeremonieSoort soort) {
        return marriageIntakeService.findAllMarriageTypes().stream()
                .filter(mt -> mt.soort() == soort)
                .map(IntakeMarriageTypeDto::locatieId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Geen locatie gekoppeld aan " + soort));
    }

    @Test
    @WithMockUser(username = "999990019", roles = "BURGER")
    void volDossierVanIemandAnders_stuurtDoorNaarEigenDossier() {
        UUID eigenDossierId = maakDossier(CeremonieSoort.KLEIN);
        UUID anderDossierId = maakDossierVoor("999990007");
        marriageIntakeService.acceptInvitation(anderDossierId, new BurgerServiceNummer("999990202"));

        tester.startPage(DatumKiezenPage.class, dossierParams(anderDossierId));

        tester.assertRenderedPage(DeDagPage.class);
        // The links on the page point to the citizen's own dossier
        assertThat(tester.getLastResponseAsString()).contains(eigenDossierId.toString());
        tester.assertContains("Daarom ziet u nu uw eigen aanvraag");
    }

    @Test
    @WithMockUser(username = "999990020", roles = "BURGER")
    void volDossierVanAnderen_zonderEigenDossier_wordtGeweigerd() {
        UUID anderDossierId = maakDossierVoor("999990007");
        marriageIntakeService.acceptInvitation(anderDossierId, new BurgerServiceNummer("999990202"));

        tester.startPage(DatumKiezenPage.class, dossierParams(anderDossierId));

        tester.assertRenderedPage(MarriageIntakePage.class);
        tester.assertContains("U mag deze aanvraag niet bekijken");
    }

    @Test
    @WithMockUser(username = "999990020", roles = "BURGER")
    void dossierMetPlekVoorPartner_toontEerstUitnodiging() {
        UUID anderDossierId = maakDossierVoor("999990007");

        tester.startPage(DatumKiezenPage.class, dossierParams(anderDossierId));

        tester.assertRenderedPage(DossierUitnodigingPage.class);
        assertThat(marriageIntakeService.findPartnerGegevens(anderDossierId)).hasSize(1);
    }

    @Test
    @WithMockUser(username = "999990019", roles = "BURGER")
    void zonderDossierId_metEigenDossier_toontStartpaginaVanEigenDossier() {
        maakDossier(CeremonieSoort.KLEIN);

        tester.startPage(DatumKiezenPage.class, new PageParameters());

        tester.assertRenderedPage(MarriageIntakePage.class);
        // "Verder naar Mijn dag" is only shown when the citizen's own dossier was found
        tester.assertVisible(VERDER_CONTAINER);
    }

    @Test
    @WithMockUser(username = "999990020", roles = "BURGER")
    void zonderDossierId_zonderEigenDossier_toontStartpagina() {
        tester.startPage(DatumKiezenPage.class, new PageParameters());

        tester.assertRenderedPage(MarriageIntakePage.class);
        tester.assertInvisible(VERDER_CONTAINER);
    }

    private UUID maakDossierVoor(String bsn) {
        return marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, null, new BurgerServiceNummer(bsn)));
    }

    private static PageParameters dossierParams(UUID dossierId) {
        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        return params;
    }

    /** Dossier van de ingelogde burger (zie {@code @WithMockUser}), zoals de intake het aanmaakt. */
    private UUID maakDossier(CeremonieSoort soort) {
        BurgerServiceNummer ingelogd = new BurgerServiceNummer(
                requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName());
        return marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, soort, null, ingelogd));
    }

    private void maakBeschikbaarheid(long locatieId, HuwelijksType type,
                                     DayOfWeek dag, LocalTime start, LocalTime eind, int duur) {
        locationAdministrationService.createBeschikbaarheid(new CreateBeschikbaarheidDto(
                locatieId, type, dag, start, eind, duur, Bedrag.ZERO,
                LocalDate.now().minusMonths(1), LocalDate.now().plusYears(2)));
    }
}
