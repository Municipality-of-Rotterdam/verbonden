package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveExtrasDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SidebarExtraItemDto;
import nl.rotterdam.verbonden.core.features.location_administration.repository.LocatieRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.AanmaakKanaal;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.ChangeIntakeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierAccessOutcome;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietCompleetException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.IntakeMarriageTypeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.InternationaleAkteTarief;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveGetuigenDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.TrouwboekjeKeuzeDto;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.repository.AfspraakRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.repository.DossierRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.repository.GetuigenRepository;
import nl.rotterdam.verbonden.core.features.marriage_type_administration.repository.MarriageTypeLocationRepository;
import nl.rotterdam.verbonden.core.features.marriage_type_administration.repository.MarriageTypeRepository;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.repository.TrouwboekjeRepository;
import nl.rotterdam.verbonden.core.identity.PersonInfo;
import nl.rotterdam.verbonden.core.identity.PersonLookupService;
import nl.rotterdam.verbonden.core.persistence.GetuigeEntity;
import nl.rotterdam.verbonden.core.persistence.HuwelijksDossierEntity;
import nl.rotterdam.verbonden.core.persistence.HuwelijksDossiersPartnerEntity;
import nl.rotterdam.verbonden.core.persistence.MarriageTypeEntity;
import nl.rotterdam.verbonden.core.persistence.MarriageTypeLocationEntity;
import nl.rotterdam.verbonden.core.persistence.TrouwlocatieEntity;
import org.springframework.dao.OptimisticLockingFailureException;
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
    private final LocatieRepository locatieRepository;
    private final MarriageTypeLocationRepository marriageTypeLocationRepository;
    private final MarriageTypeRepository marriageTypeRepository;
    private final AfspraakRepository afspraakRepository;
    private final AfspraakPlanningService afspraakPlanningService;
    private final GetuigenRepository getuigenRepository;
    private final TrouwboekjeRepository trouwboekjeRepository;
    private final PersonLookupService personLookupService;

    MarriageIntakeServiceImpl(DossierRepository dossierRepository,
                              LocatieRepository locatieRepository,
                              MarriageTypeLocationRepository marriageTypeLocationRepository,
                              MarriageTypeRepository marriageTypeRepository,
                              AfspraakRepository afspraakRepository,
                              AfspraakPlanningService afspraakPlanningService,
                              GetuigenRepository getuigenRepository,
                              TrouwboekjeRepository trouwboekjeRepository,
                              PersonLookupService personLookupService) {
        this.dossierRepository = dossierRepository;
        this.locatieRepository = locatieRepository;
        this.marriageTypeLocationRepository = marriageTypeLocationRepository;
        this.marriageTypeRepository = marriageTypeRepository;
        this.afspraakRepository = afspraakRepository;
        this.afspraakPlanningService = afspraakPlanningService;
        this.getuigenRepository = getuigenRepository;
        this.trouwboekjeRepository = trouwboekjeRepository;
        this.personLookupService = personLookupService;
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
                            afspraakPlanningService.findEersteGelegenheid(e.getSoort()),
                            e.isActive(),
                            locatieId,
                            locatieNaam
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartnerGegevensDto> findPartnerGegevens(UUID dossierId) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        List<PartnerGegevensDto> result = new ArrayList<>();
        for (HuwelijksDossiersPartnerEntity partner : dossier.getPartners()) {
            result.add(convertToDto(partner));
        }
        return result;
    }

    private PartnerGegevensDto convertToDto(HuwelijksDossiersPartnerEntity partner) {
        BurgerServiceNummer bsn = partner.getBsn();
        if (bsn == null) {
            // Partner zonder BSN: de persoonsgegevens zijn door een medewerker ingevoerd
            return new PartnerGegevensDto(partner.getVolgorde(), null, partner.getBuitenlandsPersoonsnummer(),
                    partner.getAchternaam(), partner.getVoornamen(), partner.getGeboortedatum(),
                    partner.getGeboorteplaats(), partner.getNationaliteit(), partner.getBurgerlijkeStaat(),
                    partner.getTelefoonnummer(), partner.getEmailadres(), partner.getGekozenAchternaam(), partner.getVersie());
        }
        Optional<PersonInfo> personInfo = personLookupService.findByBsn(bsn);
        if (personInfo.isEmpty()) {
            return new PartnerGegevensDto(partner.getVolgorde(), bsn, null, "Onbekend", bsn.getValue(), null, "",
                    "Onbekend", "Onbekend",
                    partner.getTelefoonnummer(), partner.getEmailadres(), partner.getGekozenAchternaam(), partner.getVersie());
        }
        PersonInfo info = personInfo.get();
        return new PartnerGegevensDto(partner.getVolgorde(), bsn, null, info.achternaam(), info.voornamen(),
                info.geboortedatum(), info.geboorteplaats(), info.nationaliteit(), info.burgerlijkeStaat(),
                partner.getTelefoonnummer(), partner.getEmailadres(), partner.getGekozenAchternaam(), partner.getVersie());
    }

    @Override
    @Transactional
    public UUID create(CreateDossierDto dto) {
        HuwelijksDossierEntity entity = new HuwelijksDossierEntity(AanmaakKanaal.ONLINE, null);
        entity.wijzigCeremonie(dto.registratieType(), dto.ceremonieSoort(), dto.locatieId() != null
                ? locatieRepository.findById(dto.locatieId()).orElse(null)
                : null);
        if (dto.bsn1() != null) {
            entity.voegPartnerToe(dto.bsn1());
        }
        return dossierRepository.save(entity).getUuid();
    }

    @Override
    @Transactional
    public void updateIntake(UUID dossierId, ChangeIntakeDto dto) {
        HuwelijksDossierEntity e = getWijzigbaarDossier(dossierId);
        e.wijzigCeremonie(dto.registratieType(), dto.ceremonieSoort(), dto.locatieId() != null
                ? locatieRepository.findById(dto.locatieId()).orElse(null)
                : null);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findDossierIdByBsn(BurgerServiceNummer bsn) {
        return dossierRepository.findByPartners_Bsn(bsn)
                .map(HuwelijksDossierEntity::getUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public DossierAccessOutcome resolveAccess(UUID requestedDossierId, BurgerServiceNummer bsn) {
        Optional<HuwelijksDossierEntity> existingDossier = dossierRepository.findByPartners_Bsn(bsn);

        if (existingDossier.isPresent()) {
            UUID existingId = existingDossier.get().getUuid();
            if (existingId.equals(requestedDossierId)) {
                return new DossierAccessOutcome(DossierAccessOutcome.Scenario.GRANTED, requestedDossierId);
            } else {
                return new DossierAccessOutcome(DossierAccessOutcome.Scenario.SWITCHED_DOSSIER, existingId);
            }
        }

        boolean roomForPartner = dossierRepository.findByUuid(requestedDossierId)
                .map(requested -> requested.getPartners().size() < 2)
                .orElse(false);
        return roomForPartner
                ? new DossierAccessOutcome(DossierAccessOutcome.Scenario.INVITED, requestedDossierId)
                : new DossierAccessOutcome(DossierAccessOutcome.Scenario.NOT_AUTHORIZED, null);
    }

    @Override
    @Transactional
    public void acceptInvitation(UUID dossierId, BurgerServiceNummer bsn) {
        if (resolveAccess(dossierId, bsn).scenario() != DossierAccessOutcome.Scenario.INVITED) {
            throw new IllegalStateException("Toegang geweigerd: BSN is niet uitgenodigd voor dit dossier");
        }
        getDossier(dossierId).voegPartnerToe(bsn);
    }

    @Override
    @Transactional(readOnly = true)
    public DossierSamenvattingDto findByDossierId(UUID id) {
        HuwelijksDossierEntity e = getDossier(id);

        LocalDateTime datumTijdHuwelijk = findDatumTijdHuwelijk(e);

        String locatieNaam = e.getLocatie() != null ? e.getLocatie().getNaam() : null;

        BigDecimal prijs = marriageTypeRepository.findBySoort(e.getCeremonieSoort())
                .map(MarriageTypeEntity::getPrijs)
                .orElse(null);

        long aantalGetuigenIngevuld = getuigenRepository.countByDossier_IdAndNaamIsNotNull(e.getId());
        boolean getuigenBevestigd = aantalGetuigenIngevuld >= vereistAantalGetuigen(e);
        boolean getuigenGedeeltelijkIngevuld = aantalGetuigenIngevuld > 0 && !getuigenBevestigd;

        int aantalGekozenAchternamen = aantalGekozenAchternamen(e);

        List<SidebarExtraItemDto> extraItems = new ArrayList<>();
        if (e.isRingenUitwisselen()) {
            extraItems.add(new SidebarExtraItemDto("Ringen uitwisselen", null));
        }
        if (e.isMuziek()) {
            extraItems.add(new SidebarExtraItemDto("Muziek", null));
        }
        if (e.getTrouwboekje() != null) {
            extraItems.add(new SidebarExtraItemDto(e.getTrouwboekje().getNaam(), e.getTrouwboekje().getPrijs()));
        }
        if (e.isInternationaleAkte()) {
            extraItems.add(new SidebarExtraItemDto("Internationale huwelijksakte", internationaleAktePrijs(e)));
        }

        BigDecimal extrasTotaal = extraItems.stream()
                .map(SidebarExtraItemDto::prijs)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPrijs = prijs != null ? prijs.add(extrasTotaal) : (extrasTotaal.compareTo(BigDecimal.ZERO) > 0 ? extrasTotaal : null);

        return new DossierSamenvattingDto(
                e.getUuid(),
                e.getRegistratieType(),
                e.getCeremonieSoort(),
                prijs,
                datumTijdHuwelijk,
                locatieNaam,
                false,
                getuigenBevestigd,
                getuigenGedeeltelijkIngevuld,
                extraItems,
                aantalGekozenAchternamen,
                e.getPartners().size() == 2,
                totalPrijs,
                e.getStatus(),
                e.getIngediendOp(),
                isCompleet(e));
    }

    @Override
    @Transactional(readOnly = true)
    public DossierStatus findStatus(UUID dossierId) {
        return getDossier(dossierId).getStatus();
    }

    @Override
    @Transactional
    public void dienIn(UUID dossierId) {
        HuwelijksDossierEntity dossier = getWijzigbaarDossier(dossierId);
        if (!isCompleet(dossier)) {
            throw new DossierNietCompleetException(dossierId);
        }
        LocalDateTime ingediendOp = LocalDateTime.now();
        dossier.setStatus(DossierStatus.INGEDIEND);
        dossier.setIngediendOp(ingediendOp);
        dossier.setInternationaleAktePrijs(dossier.isInternationaleAkte()
                ? InternationaleAkteTarief.prijsOp(ingediendOp.toLocalDate())
                : null);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal findInternationaleAktePrijs(UUID dossierId) {
        return internationaleAktePrijs(getDossier(dossierId));
    }

    /**
     * De bij het indienen vastgelegde prijs; zolang het dossier nog niet is ingediend het tarief van vandaag.
     */
    private BigDecimal internationaleAktePrijs(HuwelijksDossierEntity dossier) {
        return dossier.getInternationaleAktePrijs() != null
                ? dossier.getInternationaleAktePrijs()
                : InternationaleAkteTarief.prijsOp(LocalDate.now());
    }

    /**
     * Een dossier is compleet wanneer datum en locatie gekozen zijn, beide partners hun achternaam
     * hebben gekozen en alle vereiste getuigen zijn ingevuld.
     */
    private boolean isCompleet(HuwelijksDossierEntity dossier) {
        return findDatumTijdHuwelijk(dossier) != null
                && dossier.getLocatie() != null
                && dossier.getPartners().size() == 2
                && aantalGekozenAchternamen(dossier) == 2
                && getuigenRepository.countByDossier_IdAndNaamIsNotNull(dossier.getId()) >= vereistAantalGetuigen(dossier);
    }

    private LocalDateTime findDatumTijdHuwelijk(HuwelijksDossierEntity dossier) {
        return afspraakRepository.findFirstByDossier_Id(dossier.getId())
                .map(a -> LocalDateTime.of(a.getDatum(), a.getStartTijd()))
                .orElse(null);
    }

    private static int vereistAantalGetuigen(HuwelijksDossierEntity dossier) {
        return dossier.getCeremonieSoort().getAantalGetuigen();
    }

    private static int aantalGekozenAchternamen(HuwelijksDossierEntity dossier) {
        return (int) dossier.getPartners().stream()
                .filter(p -> p.getGekozenAchternaam() != null)
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<LocalDate> findBeschikbareDatums(UUID dossierId, YearMonth maand) {
        return afspraakPlanningService.findBeschikbareDatums(getDossier(dossierId).getCeremonieSoort(), maand);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDateTime> findBeschikbareSlots(UUID dossierId, YearMonth maand) {
        return afspraakPlanningService.findBeschikbareSlots(getDossier(dossierId).getCeremonieSoort(), maand);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<LocalDateTime> findAllBeschikbareSlots(UUID dossierId) {
        return afspraakPlanningService.findAllBeschikbareSlots(getDossier(dossierId).getCeremonieSoort());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> findBeschikbareTijden(UUID dossierId, LocalDate datum) {
        return afspraakPlanningService.findBeschikbareTijden(getDossier(dossierId).getCeremonieSoort(), datum);
    }

    @Override
    @Transactional
    public void slaAfspraakOp(UUID dossierId, LocalDate datum, LocalTime startTijd) {
        getWijzigbaarDossier(dossierId);
        afspraakPlanningService.boekAfspraak(dossierId, datum, startTijd);
    }

    private HuwelijksDossierEntity getDossier(UUID uuid) {
        return dossierRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Dossier niet gevonden: " + uuid));
    }

    /**
     * Haalt een dossier op dat de burger nog mag wijzigen, d.w.z. met status {@link DossierStatus#CONCEPT}.
     */
    private HuwelijksDossierEntity getWijzigbaarDossier(UUID uuid) {
        HuwelijksDossierEntity dossier = getDossier(uuid);
        if (dossier.getStatus() != DossierStatus.CONCEPT) {
            throw new DossierNietWijzigbaarException(uuid, dossier.getStatus(), DossierStatus.CONCEPT);
        }
        return dossier;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetuigeDto> findGetuigen(UUID dossierId) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        return getuigenRepository.findByDossier_IdOrderByVolgnummer(dossier.getId()).stream()
                .map(e -> new GetuigeDto(
                        e.getVolgnummer(),
                        e.getNaam()))
                .toList();
    }

    @Override
    @Transactional
    public void slaGetuigenOp(UUID dossierId, List<SaveGetuigenDto> getuigen) {
        HuwelijksDossierEntity dossier = getWijzigbaarDossier(dossierId);
        getuigenRepository.deleteByDossier_Id(dossier.getId());
        for (SaveGetuigenDto dto : getuigen) {
            if (dto.naam() == null || dto.naam().isBlank()) {
                continue;
            }
            GetuigeEntity entity = new GetuigeEntity(dossier, dto.volgnummer());
            entity.setNaam(dto.naam());
            getuigenRepository.save(entity);
        }
    }

    @Override
    @Transactional
    public void slaGetuigeOp(UUID dossierId, SaveGetuigenDto dto) {
        HuwelijksDossierEntity dossier = getWijzigbaarDossier(dossierId);
        GetuigeEntity entity = getuigenRepository
                .findByDossier_IdAndVolgnummer(dossier.getId(), dto.volgnummer())
                .orElseGet(() -> new GetuigeEntity(dossier, dto.volgnummer()));
        entity.setNaam(dto.naam());
        getuigenRepository.save(entity);
    }

    @Override
    @Transactional
    public void slaPartnerGegevensOp(UUID dossierId, int volgorde, String gekozenAchternaam) {
        HuwelijksDossiersPartnerEntity partner = getPartner(getWijzigbaarDossier(dossierId), volgorde);
        partner.setGekozenAchternaam(gekozenAchternaam);
    }

    @Override
    @Transactional
    public long slaContactGegevensOp(UUID dossierId, int volgorde, long versie,
                                     Telefoonnummer telefoonnummer, Emailadres emailadres) {
        HuwelijksDossiersPartnerEntity partner = getPartner(getWijzigbaarDossier(dossierId), volgorde);
        if (partner.getVersie() != versie) {
            throw new OptimisticLockingFailureException("Contactgegevens van partner in dossier " + dossierId
                    + " zijn gewijzigd: versie " + partner.getVersie() + ", verwacht " + versie);
        }
        partner.setTelefoonnummer(telefoonnummer);
        partner.setEmailadres(emailadres);
        // Flush, zodat Hibernate de versie ophoogt (en controleert) voordat we hem teruggeven
        dossierRepository.flush();
        return partner.getVersie();
    }

    private static HuwelijksDossiersPartnerEntity getPartner(HuwelijksDossierEntity dossier, int volgorde) {
        return dossier.getPartners().stream()
                .filter(p -> p.getVolgorde() == volgorde)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Dossier " + dossier.getUuid() + " heeft geen partner " + volgorde));
    }

    @Override
    @Transactional
    public void delete(UUID dossierId) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        afspraakRepository.deleteByDossier_Id(dossier.getId());
        dossierRepository.delete(dossier);
    }

    @Override
    @Transactional(readOnly = true)
    public SaveExtrasDto findExtrasSelecties(UUID dossierId) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        Long trouwboekjeId = dossier.getTrouwboekje() != null ? dossier.getTrouwboekje().getId() : null;
        return new SaveExtrasDto(dossier.isRingenUitwisselen(), dossier.isMuziek(), trouwboekjeId, dossier.isInternationaleAkte());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrouwboekjeKeuzeDto> findActieveTrouwboekjes() {
        return trouwboekjeRepository.findActief(LocalDate.now()).stream()
                .map(e -> new TrouwboekjeKeuzeDto(e.getId(), e.getNaam(), e.getOmschrijving(), e.getAfbeelding(), e.getPrijs()))
                .toList();
    }

    @Override
    @Transactional
    public void slaExtrasOp(UUID dossierId, SaveExtrasDto dto) {
        HuwelijksDossierEntity dossier = getWijzigbaarDossier(dossierId);
        dossier.wijzigExtras(dto.ringenUitwisselen(), dto.muziek(), dto.trouwboekjeId() != null
                ? trouwboekjeRepository.findById(dto.trouwboekjeId()).orElse(null)
                : null, dto.internationaleAkte());
    }
}
