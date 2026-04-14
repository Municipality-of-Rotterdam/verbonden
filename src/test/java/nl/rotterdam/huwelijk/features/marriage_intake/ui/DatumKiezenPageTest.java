package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.location_administration.domain.HuwelijksType;
import nl.rotterdam.huwelijk.features.location_administration.repository.BeschikbaarheidRepository;
import nl.rotterdam.huwelijk.features.location_administration.repository.NietBeschikbareDagRepository;
import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.huwelijk.features.marriage_intake.repository.AfspraakRepository;
import nl.rotterdam.huwelijk.features.marriage_type_administration.repository.MarriageTypeLocationRepository;
import nl.rotterdam.huwelijk.integration_test.BaseWicketTest;
import nl.rotterdam.huwelijk.persistence.LocatieBeschikbaarheidEntity;
import nl.rotterdam.huwelijk.persistence.MarriageTypeLocationEntity;
import nl.rotterdam.huwelijk.persistence.TrouwlocatieEntity;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DatumKiezenPageTest extends BaseWicketTest {

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private BeschikbaarheidRepository beschikbaarheidRepository;

    @Autowired
    private NietBeschikbareDagRepository nietBeschikbareDagRepository;

    @Autowired
    private AfspraakRepository afspraakRepository;

    @Autowired
    private MarriageTypeLocationRepository marriageTypeLocationRepository;

    @BeforeEach
    void setUpData() {
        afspraakRepository.deleteAll();
        nietBeschikbareDagRepository.deleteAll();
        beschikbaarheidRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void paginaRendertMetKalender() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        tester.startPage(DatumKiezenPage.class, params);

        tester.assertRenderedPage(DatumKiezenPage.class);

        String response = tester.getLastResponseAsString();
        assertThat(response).contains("datum-kiezen__kalender-panel");
        assertThat(response).contains("datum-kiezen__kalender");
        assertThat(response).contains("kalender-dag");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void kalenderToontBeschikbareDagen() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        tester.startPage(DatumKiezenPage.class, params);

        tester.assertRenderedPage(DatumKiezenPage.class);

        String response = tester.getLastResponseAsString();
        assertThat(response).contains("kalender-dag--beschikbaar");
    }

    @Test
    @WithMockUser(username = "999990019", roles = "BURGER")
    void kalenderToontGeenBeschikbareDagenZonderBeschikbaarheid() {
        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        tester.startPage(DatumKiezenPage.class, params);

        tester.assertRenderedPage(DatumKiezenPage.class);

        String response = tester.getLastResponseAsString();
        assertThat(response).doesNotContain("kalender-dag--beschikbaar");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void tijdslotPanelIsVerborgenZonderDatumSelectie() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        tester.startPage(DatumKiezenPage.class, params);

        tester.assertRenderedPage(DatumKiezenPage.class);

        String response = tester.getLastResponseAsString();
        assertThat(response).doesNotContain("datum-kiezen__tijdsloten");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void maandNavigatieIsAanwezig() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        tester.startPage(DatumKiezenPage.class, params);

        tester.assertRenderedPage(DatumKiezenPage.class);

        String response = tester.getLastResponseAsString();
        assertThat(response).contains("datum-kiezen__maand-nav");
        assertThat(response).contains("datum-kiezen__maand-knop");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void eenvoudigDossierToontBeschikbareDagen() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.MIDDELGROOT);

        maakBeschikbaarheid(locatie, HuwelijksType.EENVOUDIG, DayOfWeek.WEDNESDAY,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 15);

        UUID dossierId = maakDossier(CeremonieSoort.MIDDELGROOT);

        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        tester.startPage(DatumKiezenPage.class, params);

        tester.assertRenderedPage(DatumKiezenPage.class);

        String response = tester.getLastResponseAsString();
        assertThat(response).contains("kalender-dag--beschikbaar");
    }

    @Test
    @WithMockUser(username = "999990007", roles = "BURGER")
    void volledigeDossierFlow_vanIntakeNaarDatumKiezen() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
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
        assertThat(response).contains("kalender-dag--beschikbaar");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Hulpmethoden
    // ──────────────────────────────────────────────────────────────────────────

    private TrouwlocatieEntity vindGekoppeldeLocatie(CeremonieSoort soort) {
        return marriageTypeLocationRepository.findByMarriageType_Soort(soort)
                .map(MarriageTypeLocationEntity::getLocatie)
                .orElseThrow(() -> new IllegalStateException(
                        "Geen locatie gekoppeld aan " + soort));
    }

    private UUID maakDossier(CeremonieSoort soort) {
        return marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, soort, null));
    }

    private void maakBeschikbaarheid(TrouwlocatieEntity locatie, HuwelijksType type,
                                     DayOfWeek dag, LocalTime start, LocalTime eind, int duur) {
        LocatieBeschikbaarheidEntity b = new LocatieBeschikbaarheidEntity();
        b.setLocatie(locatie);
        b.setHuwelijkstype(type);
        b.setDagVanDeWeek(dag);
        b.setStartTijd(start);
        b.setEindTijd(eind);
        b.setDuurInMinuten(duur);
        b.setPrijs(BigDecimal.ZERO);
        b.setIngangsdatum(LocalDate.now().minusMonths(1));
        b.setEinddatum(LocalDate.now().plusYears(1));
        beschikbaarheidRepository.save(b);
    }
}
