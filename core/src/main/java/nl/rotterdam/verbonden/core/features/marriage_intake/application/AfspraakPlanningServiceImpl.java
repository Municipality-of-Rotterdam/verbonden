package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.config.PlanningConfig;
import nl.rotterdam.verbonden.core.features.location_administration.domain.HuwelijksType;
import nl.rotterdam.verbonden.core.features.location_administration.repository.BeschikbaarheidRepository;
import nl.rotterdam.verbonden.core.features.location_administration.repository.LocatieRepository;
import nl.rotterdam.verbonden.core.features.location_administration.repository.NietBeschikbareDagRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.repository.AfspraakRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.repository.DossierRepository;
import nl.rotterdam.verbonden.core.features.marriage_type_administration.repository.MarriageTypeLocationRepository;
import nl.rotterdam.verbonden.core.persistence.AfspraakEntity;
import nl.rotterdam.verbonden.core.persistence.HuwelijksDossierEntity;
import nl.rotterdam.verbonden.core.persistence.LocatieBeschikbaarheidEntity;
import nl.rotterdam.verbonden.core.persistence.TrouwlocatieEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Service
class AfspraakPlanningServiceImpl implements AfspraakPlanningService {

    private final DossierRepository dossierRepository;
    private final BeschikbaarheidRepository beschikbaarheidRepository;
    private final NietBeschikbareDagRepository nietBeschikbareDagRepository;
    private final LocatieRepository locatieRepository;
    private final MarriageTypeLocationRepository marriageTypeLocationRepository;
    private final AfspraakRepository afspraakRepository;
    private final PlanningConfig planningConfig;

    AfspraakPlanningServiceImpl(DossierRepository dossierRepository,
                                BeschikbaarheidRepository beschikbaarheidRepository,
                                NietBeschikbareDagRepository nietBeschikbareDagRepository,
                                LocatieRepository locatieRepository,
                                MarriageTypeLocationRepository marriageTypeLocationRepository,
                                AfspraakRepository afspraakRepository,
                                PlanningConfig planningConfig) {
        this.dossierRepository = dossierRepository;
        this.beschikbaarheidRepository = beschikbaarheidRepository;
        this.nietBeschikbareDagRepository = nietBeschikbareDagRepository;
        this.locatieRepository = locatieRepository;
        this.marriageTypeLocationRepository = marriageTypeLocationRepository;
        this.afspraakRepository = afspraakRepository;
        this.planningConfig = planningConfig;
    }

    @Override
    @Transactional(readOnly = true)
    public LocalDate findEersteGelegenheid(CeremonieSoort ceremonieSoort) {
        HuwelijksType huwelijksType = toHuwelijksType(ceremonieSoort);
        List<TrouwlocatieEntity> locaties = resolveLocaties(ceremonieSoort);
        LocalDate datum = LocalDate.now().plusDays(1);
        LocalDate limiet = datum.plusYears(1);
        while (!datum.isAfter(limiet)) {
            for (TrouwlocatieEntity locatie : locaties) {
                if (heeftVrijSlot(locatie.getId(), huwelijksType, datum)) {
                    return datum;
                }
            }
            datum = datum.plusDays(1);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<LocalDate> findBeschikbareDatums(CeremonieSoort ceremonieSoort, YearMonth maand) {
        HuwelijksType huwelijksType = toHuwelijksType(ceremonieSoort);
        List<TrouwlocatieEntity> locaties = resolveLocaties(ceremonieSoort);

        Set<LocalDate> beschikbaar = new HashSet<>();
        LocalDate vandaag = LocalDate.now();
        LocalDate vroegste = vandaag.plusDays(planningConfig.getVanafDagen());
        LocalDate laatste = vandaag.plusDays(planningConfig.getTotDagen());
        LocalDate start = maand.atDay(1).isBefore(vroegste) ? vroegste : maand.atDay(1);
        LocalDate einde = maand.atEndOfMonth().isAfter(laatste) ? laatste : maand.atEndOfMonth();

        for (LocalDate datum = start; !datum.isAfter(einde); datum = datum.plusDays(1)) {
            for (TrouwlocatieEntity locatie : locaties) {
                if (heeftVrijSlot(locatie.getId(), huwelijksType, datum)) {
                    beschikbaar.add(datum);
                    break;
                }
            }
        }
        return beschikbaar;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDateTime> findBeschikbareSlots(CeremonieSoort ceremonieSoort, YearMonth maand) {
        LocalDate vandaag = LocalDate.now();
        LocalDate vroegste = vandaag.plusDays(planningConfig.getVanafDagen());
        LocalDate laatste = vandaag.plusDays(planningConfig.getTotDagen());
        LocalDate start = maand.atDay(1).isBefore(vroegste) ? vroegste : maand.atDay(1);
        LocalDate einde = maand.atEndOfMonth().isAfter(laatste) ? laatste : maand.atEndOfMonth();
        return vrijeSlotsTussen(ceremonieSoort, start, einde);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDateTime> findAllBeschikbareSlots(CeremonieSoort ceremonieSoort) {
        LocalDate vandaag = LocalDate.now();
        return vrijeSlotsTussen(ceremonieSoort,
                vandaag.plusDays(planningConfig.getVanafDagen()),
                vandaag.plusDays(planningConfig.getTotDagen()));
    }

    private List<LocalDateTime> vrijeSlotsTussen(CeremonieSoort ceremonieSoort, LocalDate start, LocalDate einde) {
        HuwelijksType huwelijksType = toHuwelijksType(ceremonieSoort);
        List<TrouwlocatieEntity> locaties = resolveLocaties(ceremonieSoort);

        List<LocalDateTime> slots = new ArrayList<>();
        for (LocalDate datum = start; !datum.isAfter(einde); datum = datum.plusDays(1)) {
            Set<LocalTime> tijdenVoorDatum = new TreeSet<>();
            for (TrouwlocatieEntity locatie : locaties) {
                tijdenVoorDatum.addAll(vrijeTijdslotenVoor(locatie.getId(), huwelijksType, datum));
            }
            LocalDate finalDatum = datum;
            tijdenVoorDatum.forEach(tijd -> slots.add(LocalDateTime.of(finalDatum, tijd)));
        }
        return slots;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> findBeschikbareTijden(CeremonieSoort ceremonieSoort, LocalDate datum) {
        HuwelijksType huwelijksType = toHuwelijksType(ceremonieSoort);
        List<TrouwlocatieEntity> locaties = resolveLocaties(ceremonieSoort);

        Set<LocalTime> tijden = new TreeSet<>();
        for (TrouwlocatieEntity locatie : locaties) {
            tijden.addAll(vrijeTijdslotenVoor(locatie.getId(), huwelijksType, datum));
        }
        return new ArrayList<>(tijden);
    }

    @Override
    @Transactional
    public void boekAfspraak(UUID dossierId, LocalDate datum, LocalTime startTijd) {
        HuwelijksDossierEntity dossier = dossierRepository.findByUuid(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier niet gevonden: " + dossierId));
        HuwelijksType huwelijksType = toHuwelijksType(dossier.getCeremonieSoort());
        List<TrouwlocatieEntity> locaties = resolveLocaties(dossier.getCeremonieSoort());

        for (TrouwlocatieEntity locatie : locaties) {
            List<LocatieBeschikbaarheidEntity> slots = beschikbaarheidRepository.findBeschikbareSlots(
                    locatie.getId(), huwelijksType, datum.getDayOfWeek(), datum);

            for (LocatieBeschikbaarheidEntity slot : slots) {
                if (isSlotVrij(slot, datum, startTijd)) {
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

    private List<TrouwlocatieEntity> resolveLocaties(CeremonieSoort ceremonieSoort) {
        return marriageTypeLocationRepository.findByMarriageType_Soort(ceremonieSoort)
                .map(mapping -> List.of(mapping.getLocatie()))
                .orElseGet(locatieRepository::findAll);
    }

    private boolean heeftVrijSlot(long locatieId, HuwelijksType huwelijksType, LocalDate datum) {
        if (nietBeschikbareDagRepository.existsByLocatie_IdAndDatum(locatieId, datum)) {
            return false;
        }
        List<LocatieBeschikbaarheidEntity> beschikbaarheden = beschikbaarheidRepository
                .findBeschikbareSlots(locatieId, huwelijksType, datum.getDayOfWeek(), datum);
        if (beschikbaarheden.isEmpty()) {
            return false;
        }
        for (LocatieBeschikbaarheidEntity b : beschikbaarheden) {
            List<LocalTime> slots = genereerSlots(b);
            Set<LocalTime> bezet = bezetteTijden(locatieId, datum);
            for (LocalTime slot : slots) {
                if (!bezet.contains(slot)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<LocalTime> vrijeTijdslotenVoor(long locatieId, HuwelijksType huwelijksType, LocalDate datum) {
        if (nietBeschikbareDagRepository.existsByLocatie_IdAndDatum(locatieId, datum)) {
            return List.of();
        }
        List<LocatieBeschikbaarheidEntity> beschikbaarheden = beschikbaarheidRepository
                .findBeschikbareSlots(locatieId, huwelijksType, datum.getDayOfWeek(), datum);
        Set<LocalTime> bezet = bezetteTijden(locatieId, datum);
        List<LocalTime> vrij = new ArrayList<>();
        for (LocatieBeschikbaarheidEntity b : beschikbaarheden) {
            for (LocalTime slot : genereerSlots(b)) {
                if (!bezet.contains(slot)) {
                    vrij.add(slot);
                }
            }
        }
        return vrij;
    }

    private boolean isSlotVrij(LocatieBeschikbaarheidEntity beschikbaarheid, LocalDate datum, LocalTime startTijd) {
        List<LocalTime> slots = genereerSlots(beschikbaarheid);
        if (!slots.contains(startTijd)) {
            return false;
        }
        return !bezetteTijden(beschikbaarheid.getLocatie().getId(), datum).contains(startTijd);
    }

    private List<LocalTime> genereerSlots(LocatieBeschikbaarheidEntity beschikbaarheid) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = beschikbaarheid.getStartTijd();
        int duur = beschikbaarheid.getDuurInMinuten();
        while (!current.plusMinutes(duur).isAfter(beschikbaarheid.getEindTijd())) {
            slots.add(current);
            current = current.plusMinutes(duur);
        }
        return slots;
    }

    private Set<LocalTime> bezetteTijden(long locatieId, LocalDate datum) {
        Set<LocalTime> bezet = new HashSet<>();
        for (AfspraakEntity a : afspraakRepository.findByLocatie_IdAndDatum(locatieId, datum)) {
            bezet.add(a.getStartTijd());
        }
        return bezet;
    }

    private static HuwelijksType toHuwelijksType(CeremonieSoort ceremonieSoort) {
        return switch (ceremonieSoort) {
            case KLEIN -> HuwelijksType.GRATIS;
            case MIDDELGROOT -> HuwelijksType.EENVOUDIG;
            case GROOT -> HuwelijksType.REGULIER;
        };
    }
}
