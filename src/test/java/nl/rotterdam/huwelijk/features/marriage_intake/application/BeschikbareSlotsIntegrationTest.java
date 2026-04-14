package nl.rotterdam.huwelijk.features.marriage_intake.application;

import nl.rotterdam.huwelijk.features.location_administration.domain.HuwelijksType;
import nl.rotterdam.huwelijk.features.location_administration.repository.BeschikbaarheidRepository;
import nl.rotterdam.huwelijk.features.location_administration.repository.LocatieRepository;
import nl.rotterdam.huwelijk.features.location_administration.repository.NietBeschikbareDagRepository;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.huwelijk.features.marriage_intake.repository.AfspraakRepository;
import nl.rotterdam.huwelijk.features.marriage_type_administration.repository.MarriageTypeLocationRepository;
import nl.rotterdam.huwelijk.integration_test.HuwelijkIntegrationTest;
import nl.rotterdam.huwelijk.persistence.AfspraakEntity;
import nl.rotterdam.huwelijk.persistence.HuwelijksDossierEntity;
import nl.rotterdam.huwelijk.persistence.LocatieBeschikbaarheidEntity;
import nl.rotterdam.huwelijk.persistence.LocatieNietBeschikbareDagEntity;
import nl.rotterdam.huwelijk.persistence.MarriageTypeLocationEntity;
import nl.rotterdam.huwelijk.persistence.TrouwlocatieEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

@HuwelijkIntegrationTest
class BeschikbareSlotsIntegrationTest {

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private LocatieRepository locatieRepository;

    @Autowired
    private BeschikbaarheidRepository beschikbaarheidRepository;

    @Autowired
    private NietBeschikbareDagRepository nietBeschikbareDagRepository;

    @Autowired
    private AfspraakRepository afspraakRepository;

    @Autowired
    private nl.rotterdam.huwelijk.features.marriage_intake.repository.DossierRepository dossierRepository;

    @Autowired
    private MarriageTypeLocationRepository marriageTypeLocationRepository;

    @BeforeEach
    void setUp() {
        afspraakRepository.deleteAll();
        nietBeschikbareDagRepository.deleteAll();
        beschikbaarheidRepository.deleteAll();
    }

    private TrouwlocatieEntity vindGekoppeldeLocatie(CeremonieSoort soort) {
        return marriageTypeLocationRepository.findByMarriageType_Soort(soort)
                .map(MarriageTypeLocationEntity::getLocatie)
                .orElseThrow(() -> new IllegalStateException(
                        "Geen locatie gekoppeld aan " + soort));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 1: Geen huwelijken gepland — alle slots beschikbaar
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void geenHuwelijkenGepland_alleSlotsBeschikbaar() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        List<LocalDateTime> slots = marriageIntakeService.findBeschikbareSlots(dossierId, maand);

        long maandagenInMaand = aantalToekomstigeWeekdagen(maand, DayOfWeek.MONDAY);
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
    void eersteWeekvolgepland_geenBeschikbaarheidOpDieDag() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(9, 20), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        vulAlleSlotsOp(locatie, dossierId, volgendeMaandag, LocalTime.of(9, 0), LocalTime.of(9, 20), 10);

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(dossierId, volgendeMaandag);
        assertThat(tijden).isEmpty();

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(dossierId, maand);
        assertThat(datums).doesNotContain(volgendeMaandag);
    }

    @Test
    void tweeWekenVolGepland_resterendeDagenNogBeschikbaar() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(9, 20), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        LocalDate dag1 = volgendeMaandag;
        LocalDate dag2 = volgendeMaandag.plusWeeks(1);
        vulAlleSlotsOp(locatie, dossierId, dag1, LocalTime.of(9, 0), LocalTime.of(9, 20), 10);
        vulAlleSlotsOp(locatie, dossierId, dag2, LocalTime.of(9, 0), LocalTime.of(9, 20), 10);

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(dossierId, maand);
        assertThat(datums).doesNotContain(dag1, dag2);

        long resterendeMaandagen = aantalToekomstigeWeekdagen(maand, DayOfWeek.MONDAY) - 2;
        if (resterendeMaandagen > 0) {
            assertThat(datums).hasSize((int) resterendeMaandagen);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Scenario 3: Uitgeschakelde dagen worden uitgesloten
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void uitgeschakeldeDag_geenSlots() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        maakNietBeschikbareDag(locatie, volgendeMaandag, "Onderhoud");

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(dossierId, volgendeMaandag);
        assertThat(tijden).isEmpty();

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(dossierId, maand);
        assertThat(datums).doesNotContain(volgendeMaandag);
    }

    @Test
    void uitgeschakeldeDag_andereDagenNogBeschikbaar() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        LocalDate maandagDaarnaast = volgendeMaandag.plusWeeks(1);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        maakNietBeschikbareDag(locatie, volgendeMaandag, "Feestdag");

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
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.MIDDELGROOT);
        LocalDate volgendeDinsdag = volgendeWeekdag(DayOfWeek.TUESDAY);
        YearMonth maand = YearMonth.from(volgendeDinsdag);

        maakBeschikbaarheid(locatie, HuwelijksType.EENVOUDIG, DayOfWeek.TUESDAY,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 15);

        UUID dossierId = maakDossier(CeremonieSoort.MIDDELGROOT);

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(dossierId, maand);
        assertThat(datums).isNotEmpty();
        assertThat(datums).allMatch(d -> d.getDayOfWeek() == DayOfWeek.TUESDAY);
        assertThat(datums).allMatch(d -> d.isAfter(LocalDate.now()));
    }

    @Test
    void findBeschikbareSlots_geeftDatumTijdCombinaties() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.MIDDELGROOT);
        LocalDate volgendeDinsdag = volgendeWeekdag(DayOfWeek.TUESDAY);
        YearMonth maand = YearMonth.from(volgendeDinsdag);

        maakBeschikbaarheid(locatie, HuwelijksType.EENVOUDIG, DayOfWeek.TUESDAY,
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
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);
        maakBeschikbaarheid(locatie, HuwelijksType.REGULIER, DayOfWeek.MONDAY,
                LocalTime.of(14, 0), LocalTime.of(16, 0), 30);

        UUID gratisDossier = maakDossier(CeremonieSoort.KLEIN);

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(gratisDossier, volgendeMaandag);
        assertThat(tijden).isNotEmpty();
        assertThat(tijden).allMatch(t -> !t.isBefore(LocalTime.of(9, 0)) && t.isBefore(LocalTime.of(10, 0)));
        assertThat(tijden).noneMatch(t -> !t.isBefore(LocalTime.of(14, 0)));
    }

    @Test
    void regulierDossier_zietAlleenReguliereSlots() {
        List<TrouwlocatieEntity> locaties = locatieRepository.findAll();
        TrouwlocatieEntity locatie = locaties.getFirst();
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);
        maakBeschikbaarheid(locatie, HuwelijksType.REGULIER, DayOfWeek.MONDAY,
                LocalTime.of(14, 0), LocalTime.of(16, 0), 30);

        UUID regulierDossier = maakDossier(CeremonieSoort.GROOT);

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(regulierDossier, volgendeMaandag);
        assertThat(tijden).isNotEmpty();
        assertThat(tijden).allMatch(t -> !t.isBefore(LocalTime.of(14, 0)));
    }

    @Test
    void eenvoudigDossier_zietAlleenEenvoudigeSlots() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.MIDDELGROOT);
        LocalDate volgendeWoensdag = volgendeWeekdag(DayOfWeek.WEDNESDAY);

        maakBeschikbaarheid(locatie, HuwelijksType.EENVOUDIG, DayOfWeek.WEDNESDAY,
                LocalTime.of(10, 0), LocalTime.of(11, 0), 15);
        maakBeschikbaarheid(locatie, HuwelijksType.REGULIER, DayOfWeek.WEDNESDAY,
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
        List<TrouwlocatieEntity> locaties = locatieRepository.findAll();
        assertThat(locaties).hasSizeGreaterThanOrEqualTo(2);

        TrouwlocatieEntity locatie1 = locaties.get(0);
        TrouwlocatieEntity locatie2 = locaties.get(1);
        LocalDate volgendeDonderdag = volgendeWeekdag(DayOfWeek.THURSDAY);

        maakBeschikbaarheid(locatie1, HuwelijksType.REGULIER, DayOfWeek.THURSDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 30);
        maakBeschikbaarheid(locatie2, HuwelijksType.REGULIER, DayOfWeek.THURSDAY,
                LocalTime.of(14, 0), LocalTime.of(15, 0), 30);

        UUID dossier = maakDossier(CeremonieSoort.GROOT);

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(dossier, volgendeDonderdag);
        assertThat(tijden).contains(LocalTime.of(9, 0), LocalTime.of(9, 30));
        assertThat(tijden).contains(LocalTime.of(14, 0), LocalTime.of(14, 30));
    }

    @Test
    void kleinDossier_zietAlleenSlotsBijGekoppeldeLocatie() {
        TrouwlocatieEntity gekoppeld = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);
        TrouwlocatieEntity andere = locatieRepository.findAll().stream()
                .filter(l -> !l.getId().equals(gekoppeld.getId()))
                .findFirst().orElseThrow();
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);

        maakBeschikbaarheid(gekoppeld, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);
        maakBeschikbaarheid(andere, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
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
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(9, 30), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        List<LocalTime> tijdenVoor = marriageIntakeService.findBeschikbareTijden(dossierId, volgendeMaandag);
        assertThat(tijdenVoor).containsExactly(LocalTime.of(9, 0), LocalTime.of(9, 10), LocalTime.of(9, 20));

        planAfspraak(locatie, dossierId, volgendeMaandag, LocalTime.of(9, 0), LocalTime.of(9, 10));

        List<LocalTime> tijdenNa = marriageIntakeService.findBeschikbareTijden(dossierId, volgendeMaandag);
        assertThat(tijdenNa).containsExactly(LocalTime.of(9, 10), LocalTime.of(9, 20));
        assertThat(tijdenNa).doesNotContain(LocalTime.of(9, 0));
    }

    @Test
    void alleAfsprakenGepland_geenSlotsMeer() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(9, 20), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);
        vulAlleSlotsOp(locatie, dossierId, volgendeMaandag, LocalTime.of(9, 0), LocalTime.of(9, 20), 10);

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(dossierId, volgendeMaandag);
        assertThat(tijden).isEmpty();

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(dossierId, maand);
        assertThat(datums).doesNotContain(volgendeMaandag);
    }

    @Test
    void afspraakOpEenSlot_andereSlotsBlijvenBeschikbaar() {
        TrouwlocatieEntity locatie = vindGekoppeldeLocatie(CeremonieSoort.KLEIN);
        LocalDate volgendeMaandag = volgendeWeekdag(DayOfWeek.MONDAY);
        YearMonth maand = YearMonth.from(volgendeMaandag);

        maakBeschikbaarheid(locatie, HuwelijksType.GRATIS, DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 10);

        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);
        planAfspraak(locatie, dossierId, volgendeMaandag, LocalTime.of(9, 0), LocalTime.of(9, 10));

        List<LocalTime> tijden = marriageIntakeService.findBeschikbareTijden(dossierId, volgendeMaandag);
        assertThat(tijden).doesNotContain(LocalTime.of(9, 0));
        assertThat(tijden).contains(LocalTime.of(9, 10), LocalTime.of(9, 20));

        Set<LocalDate> datums = marriageIntakeService.findBeschikbareDatums(dossierId, maand);
        assertThat(datums).contains(volgendeMaandag);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Hulpmethoden
    // ──────────────────────────────────────────────────────────────────────────

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

    private void maakNietBeschikbareDag(TrouwlocatieEntity locatie, LocalDate datum, String reden) {
        LocatieNietBeschikbareDagEntity dag = new LocatieNietBeschikbareDagEntity();
        dag.setLocatie(locatie);
        dag.setDatum(datum);
        dag.setReden(reden);
        dag.setLaatsteWijzigDatum(LocalDateTime.now());
        dag.setUserid("test");
        nietBeschikbareDagRepository.save(dag);
    }

    private void planAfspraak(TrouwlocatieEntity locatie, UUID dossierId, LocalDate datum,
                              LocalTime start, LocalTime eind) {
        HuwelijksDossierEntity dossier = dossierRepository.findByUuid(dossierId).orElseThrow();
        AfspraakEntity afspraak = new AfspraakEntity();
        afspraak.setDossier(dossier);
        afspraak.setLocatie(locatie);
        afspraak.setDatum(datum);
        afspraak.setStartTijd(start);
        afspraak.setEindTijd(eind);
        afspraakRepository.save(afspraak);
    }

    private void vulAlleSlotsOp(TrouwlocatieEntity locatie, UUID dossierId,
                                LocalDate datum, LocalTime start, LocalTime eind, int duur) {
        LocalTime current = start;
        while (current.plusMinutes(duur).compareTo(eind) <= 0) {
            planAfspraak(locatie, dossierId, datum, current, current.plusMinutes(duur));
            current = current.plusMinutes(duur);
        }
    }

    private static LocalDate volgendeWeekdag(DayOfWeek dag) {
        return LocalDate.now().with(TemporalAdjusters.next(dag));
    }

    private long aantalToekomstigeWeekdagen(YearMonth maand, DayOfWeek dag) {
        LocalDate vandaag = LocalDate.now();
        return maand.atDay(1).datesUntil(maand.atEndOfMonth().plusDays(1))
                .filter(d -> d.getDayOfWeek() == dag)
                .filter(d -> d.isAfter(vandaag))
                .count();
    }
}
