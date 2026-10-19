package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.config.PlanningConfig;
import nl.rotterdam.verbonden.core.features.location_administration.domain.HuwelijksType;
import nl.rotterdam.verbonden.core.features.location_administration.repository.BeschikbaarheidRepository;
import nl.rotterdam.verbonden.core.features.location_administration.repository.LocatieRepository;
import nl.rotterdam.verbonden.core.features.location_administration.repository.NietBeschikbareDagRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BezetTijdslot;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.repository.AfspraakRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.repository.DossierRepository;
import nl.rotterdam.verbonden.core.features.marriage_type_administration.repository.MarriageTypeLocationRepository;
import nl.rotterdam.verbonden.core.persistence.AfspraakEntity;
import nl.rotterdam.verbonden.core.persistence.HuwelijksDossierEntity;
import nl.rotterdam.verbonden.core.persistence.LocatieBeschikbaarheidEntity;
import nl.rotterdam.verbonden.core.persistence.TrouwlocatieEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;

@Service
class AfspraakPlanningServiceImpl implements AfspraakPlanningService {

    private final DossierRepository dossierRepository;
    private final BeschikbaarheidRepository beschikbaarheidRepository;
    private final NietBeschikbareDagRepository nietBeschikbareDagRepository;
    private final LocatieRepository locatieRepository;
    private final MarriageTypeLocationRepository marriageTypeLocationRepository;
    private final AfspraakRepository afspraakRepository;
    private final PlanningConfig planningConfig;
    private final Clock clock;

    AfspraakPlanningServiceImpl(DossierRepository dossierRepository,
                                BeschikbaarheidRepository beschikbaarheidRepository,
                                NietBeschikbareDagRepository nietBeschikbareDagRepository,
                                LocatieRepository locatieRepository,
                                MarriageTypeLocationRepository marriageTypeLocationRepository,
                                AfspraakRepository afspraakRepository,
                                PlanningConfig planningConfig,
                                Clock clock) {
        this.dossierRepository = dossierRepository;
        this.beschikbaarheidRepository = beschikbaarheidRepository;
        this.nietBeschikbareDagRepository = nietBeschikbareDagRepository;
        this.locatieRepository = locatieRepository;
        this.marriageTypeLocationRepository = marriageTypeLocationRepository;
        this.afspraakRepository = afspraakRepository;
        this.planningConfig = planningConfig;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public LocalDate findEersteGelegenheid(CeremonieSoort ceremonieSoort) {
        LocalDate van = vroegsteDatum();
        LocalDate tot = laatsteDatum();
        Planning planning = laadPlanning(ceremonieSoort, van, tot, null);
        for (LocalDate datum = van; !datum.isAfter(tot); datum = datum.plusDays(1)) {
            if (!planning.vrijeTijden(datum).isEmpty()) {
                return datum;
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<LocalDate> findBeschikbareDatums(UUID dossierId, YearMonth maand) {
        Set<LocalDate> beschikbaar = new HashSet<>();
        for (LocalDateTime slot : findBeschikbareSlots(dossierId, maand)) {
            beschikbaar.add(slot.toLocalDate());
        }
        return beschikbaar;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDateTime> findBeschikbareSlots(UUID dossierId, YearMonth maand) {
        LocalDate vroegste = vroegsteDatum();
        LocalDate laatste = laatsteDatum();
        LocalDate start = maand.atDay(1).isBefore(vroegste) ? vroegste : maand.atDay(1);
        LocalDate einde = maand.atEndOfMonth().isAfter(laatste) ? laatste : maand.atEndOfMonth();
        return vrijeSlotsTussen(getDossier(dossierId), start, einde);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDateTime> findAllBeschikbareSlots(UUID dossierId) {
        return vrijeSlotsTussen(getDossier(dossierId), vroegsteDatum(), laatsteDatum());
    }

    /** De eerste dag van de planningsperiode: eerder kan een burger geen datum kiezen. */
    private LocalDate vroegsteDatum() {
        return LocalDate.now(clock).plusDays(planningConfig.getVanafDagen());
    }

    /** De laatste dag van de planningsperiode. */
    private LocalDate laatsteDatum() {
        return LocalDate.now(clock).plusDays(planningConfig.getTotDagen());
    }

    private List<LocalDateTime> vrijeSlotsTussen(HuwelijksDossierEntity dossier, LocalDate start, LocalDate einde) {
        List<LocalDateTime> slots = new ArrayList<>();
        if (start.isAfter(einde)) {
            return slots;
        }
        Planning planning = laadPlanning(dossier.getCeremonieSoort(), start, einde, dossier);
        for (LocalDate datum = start; !datum.isAfter(einde); datum = datum.plusDays(1)) {
            LocalDate finalDatum = datum;
            planning.vrijeTijden(datum).forEach(tijd -> slots.add(LocalDateTime.of(finalDatum, tijd)));
        }
        return slots;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> findBeschikbareTijden(UUID dossierId, LocalDate datum) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        Planning planning = laadPlanning(dossier.getCeremonieSoort(), datum, datum, dossier);
        return new ArrayList<>(planning.vrijeTijden(datum));
    }

    @Override
    @Transactional
    public void boekAfspraak(UUID dossierId, LocalDate datum, LocalTime startTijd) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        Planning planning = laadPlanning(dossier.getCeremonieSoort(), datum, datum, dossier);

        for (TrouwlocatieEntity locatie : planning.locaties()) {
            for (LocatieBeschikbaarheidEntity slot : planning.beschikbaarheden(locatie.getId(), datum)) {
                if (genereerSlots(slot).contains(startTijd)
                        && !planning.bezetteTijden(locatie.getId(), datum).contains(startTijd)) {
                    LocalTime eindTijd = startTijd.plusMinutes(slot.getDuurInMinuten());

                    afspraakRepository.deleteByDossier_Id(dossier.getId());

                    AfspraakEntity afspraak = new AfspraakEntity(dossier);
                    afspraak.setLocatie(locatie);
                    afspraak.setDatum(datum);
                    afspraak.setStartTijd(startTijd);
                    afspraak.setEindTijd(eindTijd);
                    afspraakRepository.save(afspraak);
                    return;
                }
            }
        }
        throw new IllegalStateException("Geen beschikbaar tijdslot gevonden voor " + datum + " " + startTijd);
    }

    private HuwelijksDossierEntity getDossier(UUID dossierId) {
        return dossierRepository.findByUuid(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier niet gevonden: " + dossierId));
    }

    private List<TrouwlocatieEntity> resolveLocaties(CeremonieSoort ceremonieSoort) {
        return marriageTypeLocationRepository.findByMarriageType_Soort(ceremonieSoort)
                .map(mapping -> List.of(mapping.getLocatie()))
                .orElseGet(locatieRepository::findAll);
    }

    /**
     * Haalt in drie queries alles op wat nodig is om de vrije tijdsloten van {@code van} tot en met {@code tot} te
     * bepalen, in plaats van een paar queries per dag. De afspraak van {@code eigenDossier} (indien niet
     * {@code null}) telt niet als bezet: dat dossier mag zijn eigen tijdslot opnieuw kiezen.
     */
    private Planning laadPlanning(CeremonieSoort ceremonieSoort, LocalDate van, LocalDate tot,
                                  HuwelijksDossierEntity eigenDossier) {
        List<TrouwlocatieEntity> locaties = resolveLocaties(ceremonieSoort);
        if (locaties.isEmpty()) {
            return new Planning(locaties, Map.of(), Set.of(), Map.of());
        }
        List<Long> locatieIds = locaties.stream().map(TrouwlocatieEntity::getId).toList();

        Map<Long, List<LocatieBeschikbaarheidEntity>> beschikbaarhedenPerLocatie = beschikbaarheidRepository
                .findGeldigTussen(locatieIds, toHuwelijksType(ceremonieSoort), van, tot).stream()
                .collect(groupingBy(b -> b.getLocatie().getId(), toUnmodifiableList()));
        Set<LocatieDatum> nietBeschikbaar = nietBeschikbareDagRepository
                .findByLocatie_IdInAndDatumBetween(locatieIds, van, tot).stream()
                .map(d -> new LocatieDatum(d.getLocatie().getId(), d.getDatum()))
                .collect(toUnmodifiableSet());
        // De afspraak van een geannuleerd dossier blijft bewaard, maar houdt het tijdslot niet bezet
        Map<LocatieDatum, Set<LocalTime>> bezet = afspraakRepository
                .findBezetteTijdsloten(locatieIds, van, tot, DossierStatus.ACTIEF).stream()
                .filter(b -> eigenDossier == null || eigenDossier.getId() != b.dossierId())
                .collect(groupingBy(b -> new LocatieDatum(b.locatieId(), b.datum()),
                        mapping(BezetTijdslot::startTijd, toUnmodifiableSet())));

        return new Planning(locaties, beschikbaarhedenPerLocatie, nietBeschikbaar, bezet);
    }

    private static List<LocalTime> genereerSlots(LocatieBeschikbaarheidEntity beschikbaarheid) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = beschikbaarheid.getStartTijd();
        int duur = beschikbaarheid.getDuurInMinuten();
        while (!current.plusMinutes(duur).isAfter(beschikbaarheid.getEindTijd())) {
            slots.add(current);
            current = current.plusMinutes(duur);
        }
        return slots;
    }

    private record LocatieDatum(long locatieId, LocalDate datum) {
    }

    /**
     * Beschikbaarheden, niet-beschikbare dagen en bezette tijden van een aantal locaties in een periode, zoals
     * geladen door {@link #laadPlanning}.
     */
    private record Planning(List<TrouwlocatieEntity> locaties,
                            Map<Long, List<LocatieBeschikbaarheidEntity>> beschikbaarhedenPerLocatie,
                            Set<LocatieDatum> nietBeschikbaar,
                            Map<LocatieDatum, Set<LocalTime>> bezet) {

        Planning {
            locaties = List.copyOf(locaties);
            beschikbaarhedenPerLocatie = Map.copyOf(beschikbaarhedenPerLocatie);
            nietBeschikbaar = Set.copyOf(nietBeschikbaar);
            bezet = Map.copyOf(bezet);
        }

        /** De beschikbaarheden die op {@code datum} voor de locatie gelden, leeg op een niet-beschikbare dag. */
        List<LocatieBeschikbaarheidEntity> beschikbaarheden(long locatieId, LocalDate datum) {
            if (nietBeschikbaar.contains(new LocatieDatum(locatieId, datum))) {
                return List.of();
            }
            return beschikbaarhedenPerLocatie.getOrDefault(locatieId, List.of()).stream()
                    .filter(b -> b.getDagVanDeWeek() == datum.getDayOfWeek()
                            && !b.getIngangsdatum().isAfter(datum)
                            && !b.getEinddatum().isBefore(datum))
                    .toList();
        }

        Set<LocalTime> bezetteTijden(long locatieId, LocalDate datum) {
            return bezet.getOrDefault(new LocatieDatum(locatieId, datum), Set.of());
        }

        /** De vrije starttijden op {@code datum}, over alle locaties samen. */
        Set<LocalTime> vrijeTijden(LocalDate datum) {
            Set<LocalTime> vrij = new TreeSet<>();
            for (TrouwlocatieEntity locatie : locaties) {
                Set<LocalTime> bezetOpDatum = bezetteTijden(locatie.getId(), datum);
                for (LocatieBeschikbaarheidEntity b : beschikbaarheden(locatie.getId(), datum)) {
                    for (LocalTime slot : genereerSlots(b)) {
                        if (!bezetOpDatum.contains(slot)) {
                            vrij.add(slot);
                        }
                    }
                }
            }
            return vrij;
        }
    }

    private static HuwelijksType toHuwelijksType(CeremonieSoort ceremonieSoort) {
        return switch (ceremonieSoort) {
            case KLEIN -> HuwelijksType.GRATIS;
            case MIDDELGROOT -> HuwelijksType.EENVOUDIG;
            case GROOT -> HuwelijksType.REGULIER;
        };
    }
}
