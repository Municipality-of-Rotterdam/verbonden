package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.features.dossier_administration.application.DossierAdministrationService;
import nl.rotterdam.verbonden.core.features.location_administration.application.LocationAdministrationService;
import nl.rotterdam.verbonden.core.features.location_administration.domain.CreateBeschikbaarheidDto;
import nl.rotterdam.verbonden.core.features.location_administration.domain.CreateNietBeschikbareDagDto;
import nl.rotterdam.verbonden.core.features.location_administration.domain.HuwelijksType;
import nl.rotterdam.verbonden.core.features.location_administration.domain.ListLocatieDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.IntakeMarriageTypeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.config.PlanningConfig;
import nl.rotterdam.verbonden.core.integration_test.InstelbareKlok;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Draait elke test op een reeks vaste datums: alle zeven weekdagen rond een maandovergang, en een jaarovergang.
 * Zo hangt de uitkomst niet af van de dag waarop de test toevallig draait.
 */
@VerbondenIntegrationTest
@Transactional
@ParameterizedClass(name = "vandaag = {0}")
@MethodSource("testdatums")
class BeschikbareSlotsIntegrationTest {

    static Stream<LocalDate> testdatums() {
        LocalDate maandagVoorMaandovergang = LocalDate.of(2026, 10, 26);
        return Stream.concat(
                Stream.iterate(maandagVoorMaandovergang, d -> d.plusDays(1)).limit(7),
                Stream.of(LocalDate.of(2026, 12, 31)));
    }

    @Parameter
    private LocalDate vandaag;

    @Autowired
    private InstelbareKlok klok;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private LocationAdministrationService locationAdministrationService;

    @Autowired
    private PlanningConfig planningConfig;

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        klok.zetOp(vandaag);
        for (ListLocatieDto locatie : locationAdministrationService.findAllLocaties()) {
            locationAdministrationService.findBeschikbaarheden(locatie.id())
                    .forEach(b -> locationAdministrationService.deleteBeschikbaarheid(b.id()));
            locationAdministrationService.findNietBeschikbareDagen(locatie.id())
                    .forEach(d -> locationAdministrationService.deleteNietBeschikbareDag(d.id()));
        }
    }

    @AfterEach
    void zetKlokTerug() {
        klok.zetTerug();
    }

    private long vindGekoppeldeLocatieId(CeremonieSoort soort) {
        return marriageIntakeService.findAllMarriageTypes().stream()
                .filter(mt -> mt.soort() == soort)
                .map(IntakeMarriageTypeDto::locatieId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Geen locatie gekoppeld aan " + soort));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 1: Geen huwelijken gepland — alle slots beschikbaar
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void geenHuwelijkenGepland_alleSlotsBeschikbaar() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        List<LocalDateTime> slots = marriageIntakeService.findBeschikbareSlots(dossierId, maand);

        long maandagenInMaand = aantalToekomstigeWeekdagen(maand);
        assertThat(slots).hasSize((int) (maandagenInMaand * 6));
        assertThat(slots).allMatch(slot -> slot.getDayOfWeek() == DayOfWeek.MONDAY);
        assertThat(slots).allMatch(slot ->
                !slot.toLocalTime().isBefore(LocalTime.of(9, 0))
                        && slot.toLocalTime().isBefore(LocalTime.of(10, 0)));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 2: Eerste 2 weken vol gepland — geen beschikbaarheid
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void eersteWeekVolgepland_geenBeschikbaarheidOpDieDag() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(9, 20), 10);

        vulAlleSlotsOp(volgendeMaandag, LocalTime.of(9, 0), LocalTime.of(9, 20));

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(dossierId, volgendeMaandag);
        assertThat(tijden).isEmpty();

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(dossierId, maand);
        assertThat(datums).doesNotContain(volgendeMaandag);
    }

    @Test
    void tweeWekenVolGepland_resterendeDagenNogBeschikbaar() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(9, 20), 10);

        @SuppressWarnings("UnnecessaryLocalVariable") LocalDate dag1 = volgendeMaandag;
        LocalDate dag2 = volgendeMaandag.plusWeeks(1);
        vulAlleSlotsOp(dag1, LocalTime.of(9, 0), LocalTime.of(9, 20));
        vulAlleSlotsOp(dag2, LocalTime.of(9, 0), LocalTime.of(9, 20));

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(dossierId, maand);
        assertThat(datums).doesNotContain(dag1, dag2);

        // dag2 kan al in de volgende maand vallen
        long volgeplandInMaand = Stream.of(dag1, dag2).filter(d -> YearMonth.from(d).equals(maand)).count();
        assertThat(datums).hasSize((int) (aantalToekomstigeWeekdagen(maand) - volgeplandInMaand));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 3: Uitgeschakelde dagen worden uitgesloten
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void uitgeschakeldeDag_geenSlots() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        maakNietBeschikbareDag(locatieId, volgendeMaandag, "Onderhoud");

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(dossierId, volgendeMaandag);
        assertThat(tijden).isEmpty();

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(dossierId, maand);
        assertThat(datums).doesNotContain(volgendeMaandag);
    }

    @Test
    void uitgeschakeldeDag_andereDagenNogBeschikbaar() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        LocalDate maandagDaarnaast = volgendeMaandag.plusWeeks(1);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        maakNietBeschikbareDag(locatieId, volgendeMaandag, "Feestdag");

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        assertThat(marriageIntakeService.findBeschikbareDatums(dossierId, maand)).doesNotContain(volgendeMaandag);
        // Vraag de maand van de volgende maandag apart op: die kan al in de volgende maand vallen
        assertThat(marriageIntakeService.findBeschikbareDatums(dossierId, YearMonth.from(maandagDaarnaast)))
                .contains(maandagDaarnaast);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 4: Jaar-maand combinaties met beschikbare momenten
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void findBeschikbareDatums_geeftAlleenDagenMetVrijeSlots() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.MIDDELGROOT);
        LocalDate volgendeDinsdag = volgendeWeekdag(DayOfWeek.TUESDAY);
        YearMonth maand = YearMonth.from(volgendeDinsdag);

        maakBeschikbaarheid(locatieId, HuwelijksType.EENVOUDIG, DayOfWeek.TUESDAY,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 15);

        UUID dossierId = maakDossier(CeremonieSoort.MIDDELGROOT);

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(dossierId, maand);
        assertThat(datums).isNotEmpty();
        assertThat(datums).allMatch(d -> d.getDayOfWeek() == DayOfWeek.TUESDAY);
        assertThat(datums).allMatch(d -> d.isAfter(vandaag));
    }

    @Test
    void findBeschikbareSlots_geeftDatumTijdCombinaties() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.MIDDELGROOT);
        LocalDate volgendeDinsdag = volgendeWeekdag(DayOfWeek.TUESDAY);
        YearMonth maand = YearMonth.from(volgendeDinsdag);

        maakBeschikbaarheid(locatieId, HuwelijksType.EENVOUDIG, DayOfWeek.TUESDAY,
                LocalTime.of(10, 0), LocalTime.of(10, 30), 15);

        UUID dossierId = maakDossier(CeremonieSoort.MIDDELGROOT);

        List<LocalDateTime> slots = marriageIntakeService.findBeschikbareSlots(dossierId, maand);
        assertThat(slots).isNotEmpty();
        assertThat(slots).allMatch(s -> s.getDayOfWeek() == DayOfWeek.TUESDAY);
        assertThat(slots).allMatch(s ->
                s.toLocalTime().equals(LocalTime.of(10, 0))
                        || s.toLocalTime().equals(LocalTime.of(10, 15)));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 5: Beschikbaarheid op basis van huwelijkstype
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void gratisDossier_zietAlleenGratisSlots() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);
        maakBeschikbaarheid(locatieId, HuwelijksType.REGULIER, DayOfWeek.MONDAY,
                LocalTime.of(14, 0), LocalTime.of(16, 0), 30);

        UUID gratisDossier = maakDossier(CeremonieSoort.KLEIN);

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(gratisDossier, volgendeMaandag);
        assertThat(tijden).isNotEmpty();
        assertThat(tijden).allMatch(t -> !t.isBefore(LocalTime.of(9, 0)) && t.isBefore(LocalTime.of(10, 0)));
        assertThat(tijden).noneMatch(t -> !t.isBefore(LocalTime.of(14, 0)));
    }

    @Test
    void regulierDossier_zietAlleenReguliereSlots() {
        List<ListLocatieDto> locaties = locationAdministrationService.findAllLocaties();
        long locatieId = locaties.getFirst().id();
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);
        maakBeschikbaarheid(locatieId, HuwelijksType.REGULIER, DayOfWeek.MONDAY,
                LocalTime.of(14, 0), LocalTime.of(16, 0), 30);

        UUID regulierDossier = maakDossier(CeremonieSoort.GROOT);

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(regulierDossier, volgendeMaandag);
        assertThat(tijden).isNotEmpty();
        assertThat(tijden).allMatch(t -> !t.isBefore(LocalTime.of(14, 0)));
    }

    @Test
    void eenvoudigDossier_zietAlleenEenvoudigeSlots() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.MIDDELGROOT);
        LocalDate volgendeWoensdag = volgendeWeekdag(DayOfWeek.WEDNESDAY);

        maakBeschikbaarheid(locatieId, HuwelijksType.EENVOUDIG, DayOfWeek.WEDNESDAY,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 15);
        maakBeschikbaarheid(locatieId, HuwelijksType.REGULIER, DayOfWeek.WEDNESDAY,
                LocalTime.of(14, 0), LocalTime.of(16, 0), 30);

        UUID eenvoudigDossier = maakDossier(CeremonieSoort.MIDDELGROOT);

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(eenvoudigDossier, volgendeWoensdag);
        assertThat(tijden).isNotEmpty();
        assertThat(tijden).allMatch(t -> !t.isBefore(LocalTime.of(10, 0)) && t.isBefore(LocalTime.of(11, 0)));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 6: Beschikbaarheid op basis van huwelijkstype en locatie
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void grootDossier_zietSlotsBijAlleLocaties() {
        List<ListLocatieDto> locaties = locationAdministrationService.findAllLocaties();
        assertThat(locaties).hasSizeGreaterThanOrEqualTo(2);

        long locatieId1 = locaties.get(0).id();
        long locatieId2 = locaties.get(1).id();
        LocalDate volgendeDonderdag = volgendeWeekdag(DayOfWeek.THURSDAY);

        maakBeschikbaarheid(locatieId1, HuwelijksType.REGULIER, DayOfWeek.THURSDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 30);
        maakBeschikbaarheid(locatieId2, HuwelijksType.REGULIER, DayOfWeek.THURSDAY,
                LocalTime.of(14, 0), LocalTime.of(15, 0), 30);

        UUID dossier = maakDossier(CeremonieSoort.GROOT);

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(dossier, volgendeDonderdag);
        assertThat(tijden).contains(LocalTime.of(9, 0), LocalTime.of(9, 30));
        assertThat(tijden).contains(LocalTime.of(14, 0), LocalTime.of(14, 30));
    }

    @Test
    void kleinDossier_zietAlleenSlotsBijGekoppeldeLocatie() {
        long gekoppeldId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        long andereId = locationAdministrationService.findAllLocaties().stream()
                .filter(l -> l.id() != gekoppeldId)
                .findFirst().orElseThrow().id();
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);

        maakBeschikbaarheid(gekoppeldId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);
        maakBeschikbaarheid(andereId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(14, 0), LocalTime.of(15, 0), 10);

        UUID dossier = maakDossier(CeremonieSoort.KLEIN);

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(dossier, volgendeMaandag);
        assertThat(tijden).isNotEmpty();
        assertThat(tijden).allMatch(t -> !t.isBefore(LocalTime.of(9, 0)) && t.isBefore(LocalTime.of(10, 0)));
        assertThat(tijden).noneMatch(t -> !t.isBefore(LocalTime.of(14, 0)));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 7: Reeds geplande afspraken verminderen beschikbaarheid
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void geplandeAfspraak_vermindertBeschikbaarheid() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(9, 30), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);
        UUID anderDossierId = maakDossier(CeremonieSoort.KLEIN);

        List<LocalTime> tijdenVoor = marriageIntakeService.findBeschikbareTijden(anderDossierId, volgendeMaandag);
        assertThat(tijdenVoor).containsExactly(LocalTime.of(9, 0), LocalTime.of(9, 10), LocalTime.of(9, 20));

        marriageIntakeService.slaAfspraakOp(dossierId, volgendeMaandag, LocalTime.of(9, 0));

        List<LocalTime> tijdenNa = marriageIntakeService.findBeschikbareTijden(anderDossierId, volgendeMaandag);
        assertThat(tijdenNa).containsExactly(LocalTime.of(9, 10), LocalTime.of(9, 20));
        assertThat(tijdenNa).doesNotContain(LocalTime.of(9, 0));
    }

    @Test
    void alleAfsprakenGepland_geenSlotsMeer() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(9, 20), 10);

        vulAlleSlotsOp(volgendeMaandag, LocalTime.of(9, 0), LocalTime.of(9, 20));

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(dossierId, volgendeMaandag);
        assertThat(tijden).isEmpty();

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(dossierId, maand);
        assertThat(datums).doesNotContain(volgendeMaandag);
    }

    @Test
    void afspraakOpEenSlot_andereSlotsBlijvenBeschikbaar() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);
        UUID anderDossierId = maakDossier(CeremonieSoort.KLEIN);
        marriageIntakeService.slaAfspraakOp(dossierId, volgendeMaandag, LocalTime.of(9, 0));

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(anderDossierId, volgendeMaandag);
        assertThat(tijden).doesNotContain(LocalTime.of(9, 0));
        assertThat(tijden).contains(LocalTime.of(9, 10), LocalTime.of(9, 20));

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(anderDossierId, maand);
        assertThat(datums).contains(volgendeMaandag);
    }

    @Test
    void aantalQueries_isOnafhankelijkVanDeLengteVanDePlanningsperiode() {
        LocalDate volgendeMaandag = maakPlanningMetAfspraakEnNietBeschikbareDag();
        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);
        List<LocalDateTime> slots = new ArrayList<>();

        long aantalQueries = aantalQueries(
                () -> slots.addAll(marriageIntakeService.findAllBeschikbareSlots(dossierId)));

        assertThat(slots).contains(volgendeMaandag.atTime(9, 10))
                .doesNotContain(volgendeMaandag.atTime(9, 0), volgendeMaandag.plusWeeks(1).atTime(9, 10));
        // Dossier, locatie bij de ceremoniesoort, en per soort gegeven één query voor de hele periode
        assertThat(aantalQueries).isLessThanOrEqualTo(6);
    }

    @Test
    void aantalQueries_beheer_beschikbareTijdenOpEenDatum() {
        LocalDate volgendeMaandag = maakPlanningMetAfspraakEnNietBeschikbareDag();
        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);
        List<LocalTime> tijden = new ArrayList<>();

        long aantalQueries = aantalQueries(
                () -> tijden.addAll(dossierAdministrationService.findBeschikbareTijden(dossierId, volgendeMaandag)));

        assertThat(tijden).contains(LocalTime.of(9, 10)).doesNotContain(LocalTime.of(9, 0));
        assertThat(aantalQueries).isLessThanOrEqualTo(6);
    }

    @Test
    void aantalQueries_beheer_afspraakWijzigen() {
        LocalDate volgendeMaandag = maakPlanningMetAfspraakEnNietBeschikbareDag();
        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);
        marriageIntakeService.slaAfspraakOp(dossierId, volgendeMaandag, LocalTime.of(9, 10));

        long aantalQueries = aantalQueries(
                () -> dossierAdministrationService.updateAfspraak(dossierId, volgendeMaandag, LocalTime.of(9, 20)));

        assertThat(marriageIntakeService.findDatumTijdHuwelijk(dossierId)).isEqualTo(volgendeMaandag.atTime(9, 20));
        // Planning (zoals hierboven), plus de oude afspraak ophalen en verwijderen en de nieuwe opslaan
        assertThat(aantalQueries).isLessThanOrEqualTo(9);
    }

    /**
     * Beschikbaarheid op maandag 9:00-10:00, een afspraak van een ander dossier op de eerstvolgende maandag om
     * 9:00 en een niet-beschikbare dag een week later, zodat alle drie de planningsqueries iets opleveren.
     *
     * @return de eerstvolgende maandag in de planningsperiode
     */
    private LocalDate maakPlanningMetAfspraakEnNietBeschikbareDag() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);
        maakNietBeschikbareDag(locatieId, volgendeMaandag.plusWeeks(1), "Feestdag");
        marriageIntakeService.slaAfspraakOp(maakDossier(CeremonieSoort.KLEIN), volgendeMaandag, LocalTime.of(9, 0));
        return volgendeMaandag;
    }

    /**
     * Het aantal SQL-statements dat {@code actie} uitvoert, inclusief het wegschrijven van wijzigingen. Wat al
     * in de persistence context zit, wordt eerst weggeschreven en vergeten, zodat het niet meetelt of uit de
     * cache komt.
     */
    private long aantalQueries(Runnable actie) {
        entityManager.flush();
        entityManager.clear();
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        try {
            statistics.clear();
            actie.run();
            entityManager.flush();
            return statistics.getPrepareStatementCount();
        } finally {
            statistics.setStatisticsEnabled(false);
        }
    }

    @Test
    void eersteGelegenheid_valtBinnenDePlanningsperiode() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);
        // Ruim meer dan een week, zodat er vóór de planningsperiode zeker een maandag ligt
        int origineleVanafDagen = planningConfig.getVanafDagen();
        planningConfig.setVanafDagen(14);
        try {
            LocalDate eersteGelegenheid = marriageIntakeService.findAllMarriageTypes().stream()
                    .filter(mt -> mt.soort() == CeremonieSoort.KLEIN)
                    .findFirst().orElseThrow()
                    .eersteGelegenheid();

            // Dezelfde dag als de eerste die de burger in de date picker kan kiezen
            assertThat(eersteGelegenheid).isEqualTo(volgendeWeekdag(DayOfWeek.MONDAY));
        } finally {
            planningConfig.setVanafDagen(origineleVanafDagen);
        }
    }

    @Test
    void eigenAfspraak_blijftVoorEigenDossierBeschikbaar() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(9, 10), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);
        marriageIntakeService.slaAfspraakOp(dossierId, volgendeMaandag, LocalTime.of(9, 0));

        assertThat(marriageIntakeService.findBeschikbareTijden(dossierId, volgendeMaandag))
                .containsExactly(LocalTime.of(9, 0));
        assertThat(marriageIntakeService.findBeschikbareDatums(dossierId, maand)).contains(volgendeMaandag);
        assertThat(marriageIntakeService.findAllBeschikbareSlots(dossierId))
                .contains(volgendeMaandag.atTime(9, 0));

        // Hetzelfde tijdslot opnieuw boeken lukt en houdt de afspraak gelijk
        marriageIntakeService.slaAfspraakOp(dossierId, volgendeMaandag, LocalTime.of(9, 0));
        assertThat(marriageIntakeService.findDatumTijdHuwelijk(dossierId)).isEqualTo(volgendeMaandag.atTime(9, 0));
    }

    @Test
    void findAllBeschikbareSlots_retourneertSlotsInPlanningsperiode() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(9, 30), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        var allSlots = marriageIntakeService.findAllBeschikbareSlots(dossierId);
        assertThat(allSlots).isNotEmpty();

        LocalDate vroegste = vandaag.plusDays(planningConfig.getVanafDagen());
        LocalDate laatste = vandaag.plusDays(planningConfig.getTotDagen());

        assertThat(allSlots).allMatch(slot -> !slot.toLocalDate().isBefore(vroegste),
                "Geen slot mag voor de vroegste planningsdatum liggen");
        assertThat(allSlots).allMatch(slot -> !slot.toLocalDate().isAfter(laatste),
                "Geen slot mag na de laatste planningsdatum liggen");
    }

    @Test
    void findAllBeschikbareSlots_leegZonderBeschikbaarheid() {
        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        var allSlots = marriageIntakeService.findAllBeschikbareSlots(dossierId);
        assertThat(allSlots).isEmpty();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Hulpmethoden
    // ──────────────────────────────────────────────────────────────────────────

    private UUID maakDossier(CeremonieSoort soort) {
        return marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, soort, null, null));
    }

    private void maakBeschikbaarheid(long locatieId, HuwelijksType type,
                                     DayOfWeek dag, LocalTime start, LocalTime eind, int duur) {
        locationAdministrationService.createBeschikbaarheid(new CreateBeschikbaarheidDto(
                locatieId, type, dag, start, eind, duur, BigDecimal.ZERO,
                vandaag.minusMonths(1), vandaag.plusYears(2)));
    }

    private void maakNietBeschikbareDag(long locatieId, LocalDate datum, String reden) {
        locationAdministrationService.createNietBeschikbareDag(
                new CreateNietBeschikbareDagDto(locatieId, datum, reden));
    }

    private void vulAlleSlotsOp(LocalDate datum, LocalTime start, LocalTime eind) {
        LocalTime current = start;
        while (!current.plusMinutes(10).isAfter(eind)) {
            UUID dossier = maakDossier(CeremonieSoort.KLEIN);
            marriageIntakeService.slaAfspraakOp(dossier, datum, current);
            current = current.plusMinutes(10);
        }
    }

    private LocalDate volgendeWeekdag(DayOfWeek dag) {
        LocalDate vroegste = vandaag.plusDays(planningConfig.getVanafDagen());
        return vroegste.with(TemporalAdjusters.nextOrSame(dag));
    }

    private long aantalToekomstigeWeekdagen(YearMonth maand) {
        LocalDate vroegste = vandaag.plusDays(planningConfig.getVanafDagen());
        return maand.atDay(1).datesUntil(maand.atEndOfMonth().plusDays(1))
                .filter(d -> d.getDayOfWeek() == DayOfWeek.MONDAY)
                .filter(d -> !d.isBefore(vroegste))
                .count();
    }
}
