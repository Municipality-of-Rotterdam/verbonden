package nl.rotterdam.huwelijk.features.marriage_intake.application;

import nl.rotterdam.huwelijk.features.location_administration.domain.HuwelijksType;
import nl.rotterdam.huwelijk.features.location_administration.repository.BeschikbaarheidRepository;
import nl.rotterdam.huwelijk.features.location_administration.repository.LocatieRepository;
import nl.rotterdam.huwelijk.features.location_administration.repository.NietBeschikbareDagRepository;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.IntakeMarriageTypeDto;
import nl.rotterdam.huwelijk.features.marriage_intake.repository.AfspraakRepository;
import nl.rotterdam.huwelijk.features.marriage_intake.repository.DossierRepository;
import nl.rotterdam.huwelijk.features.marriage_type_administration.repository.MarriageTypeLocationRepository;
import nl.rotterdam.huwelijk.features.marriage_type_administration.repository.MarriageTypeRepository;
import nl.rotterdam.huwelijk.persistence.AfspraakEntity;
import nl.rotterdam.huwelijk.persistence.HuwelijksDossierEntity;
import nl.rotterdam.huwelijk.persistence.LocatieBeschikbaarheidEntity;
import nl.rotterdam.huwelijk.persistence.MarriageTypeEntity;
import nl.rotterdam.huwelijk.persistence.MarriageTypeLocationEntity;
import nl.rotterdam.huwelijk.persistence.TrouwlocatieEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.UUID;

@Service
class MarriageIntakeServiceImpl implements MarriageIntakeService {

    private final DossierRepository dossierRepository;
    private final BeschikbaarheidRepository beschikbaarheidRepository;
    private final NietBeschikbareDagRepository nietBeschikbareDagRepository;
    private final LocatieRepository locatieRepository;
    private final MarriageTypeLocationRepository marriageTypeLocationRepository;
    private final MarriageTypeRepository marriageTypeRepository;
    private final AfspraakRepository afspraakRepository;

    MarriageIntakeServiceImpl(DossierRepository dossierRepository,
                              BeschikbaarheidRepository beschikbaarheidRepository,
                              NietBeschikbareDagRepository nietBeschikbareDagRepository,
                              LocatieRepository locatieRepository,
                              MarriageTypeLocationRepository marriageTypeLocationRepository,
                              MarriageTypeRepository marriageTypeRepository,
                              AfspraakRepository afspraakRepository) {
        this.dossierRepository = dossierRepository;
        this.beschikbaarheidRepository = beschikbaarheidRepository;
        this.nietBeschikbareDagRepository = nietBeschikbareDagRepository;
        this.locatieRepository = locatieRepository;
        this.marriageTypeLocationRepository = marriageTypeLocationRepository;
        this.marriageTypeRepository = marriageTypeRepository;
        this.afspraakRepository = afspraakRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IntakeMarriageTypeDto> findAllMarriageTypes() {
        return marriageTypeRepository.findAll().stream()
                .sorted(Comparator.comparing(MarriageTypeEntity::getSoort))
                .map(e -> {
                    Optional<MarriageTypeLocationEntity> locationMapping =
                            marriageTypeLocationRepository.findByMarriageType_Soort(e.getSoort());
                    TrouwlocatieEntity locatie = locationMapping.map(MarriageTypeLocationEntity::getLocatie).orElse(null);
                    Long locatieId = locatie != null ? locatie.getId() : null;
                    String locatieNaam = locatie != null ? locatie.getNaam() : null;
                    return new IntakeMarriageTypeDto(
                            e.getSoort(),
                            e.getTitel(),
                            e.getPrijs(),
                            e.getSoort() == CeremonieSoort.GROOT ? "Vanaf" : null,
                            Arrays.stream(e.getTekst().split("\n"))
                                    .map(String::trim)
                                    .filter(s -> !s.isEmpty())
                                    .toList(),
                            computeEersteGelegenheid(e.getSoort()),
                            e.isActive(),
                            locatieId,
                            locatieNaam
                    );
                })
                .toList();
    }

    private LocalDate computeEersteGelegenheid(CeremonieSoort ceremonieSoort) {
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
    @Transactional
    public UUID create(CreateDossierDto dto) {
        HuwelijksDossierEntity entity = new HuwelijksDossierEntity();
        entity.setRegistratieType(dto.registratieType());
        entity.setCeremonieSoort(dto.ceremonieSoort());
        if (dto.locatieId() != null) {
            locatieRepository.findById(dto.locatieId()).ifPresent(entity::setLocatie);
        }
        return dossierRepository.save(entity).getUuid();
    }

    @Override
    @Transactional
    public void updateCeremonie(UUID dossierId, CeremonieSoort ceremonieSoort) {
        getDossier(dossierId).setCeremonieSoort(ceremonieSoort);
    }

    @Override
    @Transactional(readOnly = true)
    public DossierSamenvattingDto findByDossierId(UUID id) {
        HuwelijksDossierEntity e = getDossier(id);

        LocalDate datumHuwelijk = afspraakRepository.findFirstByDossier_Id(e.getId())
                .map(AfspraakEntity::getDatum)
                .orElse(null);

        String locatieNaam = e.getLocatie() != null ? e.getLocatie().getNaam() : null;

        BigDecimal prijs = marriageTypeRepository.findBySoort(e.getCeremonieSoort())
                .map(MarriageTypeEntity::getPrijs)
                .orElse(null);

        return new DossierSamenvattingDto(
                e.getUuid(),
                e.getRegistratieType(),
                e.getCeremonieSoort(),
                prijs,
                datumHuwelijk,
                locatieNaam,
                false,
                false,
                List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<LocalDate> findBeschikbareDatums(UUID dossierId, YearMonth maand) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        HuwelijksType huwelijksType = toHuwelijksType(dossier.getCeremonieSoort());
        List<TrouwlocatieEntity> locaties = resolveLocaties(dossier.getCeremonieSoort());

        Set<LocalDate> beschikbaar = new HashSet<>();
        LocalDate vandaag = LocalDate.now();
        LocalDate start = maand.atDay(1);
        LocalDate einde = maand.atEndOfMonth();

        for (LocalDate datum = start; !datum.isAfter(einde); datum = datum.plusDays(1)) {
            if (!datum.isAfter(vandaag)) {
                continue;
            }
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
    public List<LocalDateTime> findBeschikbareSlots(UUID dossierId, YearMonth maand) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        HuwelijksType huwelijksType = toHuwelijksType(dossier.getCeremonieSoort());
        List<TrouwlocatieEntity> locaties = resolveLocaties(dossier.getCeremonieSoort());

        List<LocalDateTime> slots = new ArrayList<>();
        LocalDate vandaag = LocalDate.now();
        LocalDate start = maand.atDay(1);
        LocalDate einde = maand.atEndOfMonth();

        for (LocalDate datum = start; !datum.isAfter(einde); datum = datum.plusDays(1)) {
            if (!datum.isAfter(vandaag)) {
                continue;
            }
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
    public Collection<LocalDateTime> findAllBeschikbareSlots(UUID dossierId) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        HuwelijksType huwelijksType = toHuwelijksType(dossier.getCeremonieSoort());
        List<TrouwlocatieEntity> locaties = resolveLocaties(dossier.getCeremonieSoort());

        List<LocalDateTime> slots = new ArrayList<>();
        LocalDate vandaag = LocalDate.now();
        LocalDate einde = vandaag.plusMonths(18);

        for (LocalDate datum = vandaag.plusDays(1); !datum.isAfter(einde); datum = datum.plusDays(1)) {
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
    public List<LocalTime> findBeschikbareTijden(UUID dossierId, LocalDate datum) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        HuwelijksType huwelijksType = toHuwelijksType(dossier.getCeremonieSoort());
        List<TrouwlocatieEntity> locaties = resolveLocaties(dossier.getCeremonieSoort());

        Set<LocalTime> tijden = new TreeSet<>();
        for (TrouwlocatieEntity locatie : locaties) {
            tijden.addAll(vrijeTijdslotenVoor(locatie.getId(), huwelijksType, datum));
        }
        return new ArrayList<>(tijden);
    }

    @Override
    @Transactional
    public void slaAfspraakOp(UUID dossierId, LocalDate datum, LocalTime startTijd) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        HuwelijksType huwelijksType = toHuwelijksType(dossier.getCeremonieSoort());
        List<TrouwlocatieEntity> locaties = resolveLocaties(dossier.getCeremonieSoort());

        for (TrouwlocatieEntity locatie : locaties) {
            List<LocatieBeschikbaarheidEntity> slots = beschikbaarheidRepository.findBeschikbareSlots(
                    locatie.getId(), huwelijksType, datum.getDayOfWeek(), datum);

            for (LocatieBeschikbaarheidEntity slot : slots) {
                if (isSlotVrij(slot, datum, startTijd)) {
                    LocalTime eindTijd = startTijd.plusMinutes(slot.getDuurInMinuten());

                    afspraakRepository.deleteByDossier_Id(dossier.getId());

                    AfspraakEntity afspraak = new AfspraakEntity();
                    afspraak.setDossier(dossier);
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

    private HuwelijksDossierEntity getDossier(UUID uuid) {
        return dossierRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Dossier niet gevonden: " + uuid));
    }

    private static HuwelijksType toHuwelijksType(CeremonieSoort ceremonieSoort) {
        return switch (ceremonieSoort) {
            case KLEIN -> HuwelijksType.GRATIS;
            case MIDDELGROOT -> HuwelijksType.EENVOUDIG;
            case GROOT -> HuwelijksType.REGULIER;
        };
    }
}

