package nl.rotterdam.verbonden.core.features.marriage_intake.application;

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
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@VerbondenIntegrationTest
@Transactional
class BeschikbareSlotsIntegrationTest {

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private LocationAdministrationService locationAdministrationService;

    @Autowired
    private PlanningConfig planningConfig;

    @BeforeEach
    void setUp() {
        for (ListLocatieDto locatie : locationAdministrationService.findAllLocaties()) {
            locationAdministrationService.findBeschikbaarheden(locatie.id())
                    .forEach(b -> locationAdministrationService.deleteBeschikbaarheid(b.id()));
            locationAdministrationService.findNietBeschikbareDagen(locatie.id())
                    .forEach(d -> locationAdministrationService.deleteNietBeschikbareDag(d.id()));
        }
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

        LocalDate dag1 = volgendeMaandag;
        LocalDate dag2 = volgendeMaandag.plusWeeks(1);
        vulAlleSlotsOp(dag1, LocalTime.of(9, 0), LocalTime.of(9, 20));
        vulAlleSlotsOp(dag2, LocalTime.of(9, 0), LocalTime.of(9, 20));

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(dossierId, maand);
        assertThat(datums).doesNotContain(dag1, dag2);

        long resterendeMaandagen = aantalToekomstigeWeekdagen(maand) - 2;
        if (resterendeMaandagen > 0) {
            assertThat(datums).hasSize((int) resterendeMaandagen);
        }
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

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(dossierId, maand);
        assertThat(datums).doesNotContain(volgendeMaandag);
        if (maand.equals(YearMonth.from(maandagDaarnaast))) {
            assertThat(datums).contains(maandagDaarnaast);
        }
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
        assertThat(datums).allMatch(d -> d.isAfter(LocalDate.now()));
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

        List<LocalTime> tijdenVoor = marriageIntakeService.findBeschikbareTijden(dossierId, volgendeMaandag);
        assertThat(tijdenVoor).containsExactly(LocalTime.of(9, 0), LocalTime.of(9, 10), LocalTime.of(9, 20));

        marriageIntakeService.slaAfspraakOp(dossierId, volgendeMaandag, LocalTime.of(9, 0));

        List<LocalTime> tijdenNa = marriageIntakeService.findBeschikbareTijden(dossierId, volgendeMaandag);
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
        marriageIntakeService.slaAfspraakOp(dossierId, volgendeMaandag, LocalTime.of(9, 0));

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(dossierId, volgendeMaandag);
        assertThat(tijden).doesNotContain(LocalTime.of(9, 0));
        assertThat(tijden).contains(LocalTime.of(9, 10), LocalTime.of(9, 20));

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(dossierId, maand);
        assertThat(datums).contains(volgendeMaandag);
    }

    @Test
    void findAllBeschikbareSlots_retourneertSlotsInPlanningsperiode() {
        long locatieId = vindGekoppeldeLocatieId(CeremonieSoort.KLEIN);

        maakBeschikbaarheid(locatieId, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(9, 30), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        var allSlots = marriageIntakeService.findAllBeschikbareSlots(dossierId);
        assertThat(allSlots).isNotEmpty();

        LocalDate vandaag = LocalDate.now();
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
                LocalDate.now().minusMonths(1), LocalDate.now().plusYears(2)));
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
        LocalDate vroegste = LocalDate.now().plusDays(planningConfig.getVanafDagen());
        return vroegste.with(TemporalAdjusters.nextOrSame(dag));
    }

    private long aantalToekomstigeWeekdagen(YearMonth maand) {
        LocalDate vroegste = LocalDate.now().plusDays(planningConfig.getVanafDagen());
        return maand.atDay(1).datesUntil(maand.atEndOfMonth().plusDays(1))
                .filter(d -> d.getDayOfWeek() == DayOfWeek.MONDAY)
                .filter(d -> !d.isBefore(vroegste))
                .count();
    }
}
