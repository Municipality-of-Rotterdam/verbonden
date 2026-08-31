package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class DatumKiezenPageTest extends BaseWicketTest {

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

    private UUID maakDossier(CeremonieSoort soort) {
        return marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, soort, null, null));
    }

    private void maakBeschikbaarheid(long locatieId, HuwelijksType type,
                                     DayOfWeek dag, LocalTime start, LocalTime eind, int duur) {
        locationAdministrationService.createBeschikbaarheid(new CreateBeschikbaarheidDto(
                locatieId, type, dag, start, eind, duur, BigDecimal.ZERO,
                LocalDate.now().minusMonths(1), LocalDate.now().plusYears(2)));
    }
}
