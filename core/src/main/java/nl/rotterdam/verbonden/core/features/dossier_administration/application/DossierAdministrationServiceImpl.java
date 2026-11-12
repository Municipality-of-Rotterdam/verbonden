package nl.rotterdam.verbonden.core.features.dossier_administration.application;

import nl.rotterdam.verbonden.core.features.betaling_administration.application.BetalingAdministrationService;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.BetaalmutatieNietMogelijkException;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateBetaalmutatieDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListBetaalmutatieDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStatus;
import nl.rotterdam.verbonden.core.persistence.BetaalmutatieEntity;
import nl.rotterdam.verbonden.core.persistence.BetalingEntity;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.BestandNietToegestaanException;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangeCeremonieDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangeExtrasDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangePartnerDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateBalieDossierDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateDossierBestandDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DetailPartnerDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierBestandDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierBestandType;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierDetailDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.VrijeTrouwlocatieDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.InzageActie;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListDossierDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.PartnerHeeftAlDossierException;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.PartnerNietInBrpException;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.PersoonsgegevensDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.repository.DossierAdministrationRepository;
import nl.rotterdam.verbonden.core.features.dossier_administration.repository.DossierBestandRepository;
import nl.rotterdam.verbonden.core.features.dossier_administration.repository.DossierInzageRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.AfspraakPlanningService;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.AanmaakKanaal;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException;
import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SidebarExtraItemDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.InternationaleAkteTarief;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveGetuigenDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveVrijeTrouwlocatieDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.TrouwboekjeKeuzeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.repository.GetuigenRepository;
import nl.rotterdam.verbonden.core.features.marriage_type_administration.repository.MarriageTypeLocationRepository;
import nl.rotterdam.verbonden.core.features.location_administration.repository.LocatieRepository;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.repository.TrouwboekjeRepository;
import nl.rotterdam.verbonden.core.identity.CurrentUserProvider;
import nl.rotterdam.verbonden.core.identity.PersonLookupService;
import nl.rotterdam.verbonden.core.persistence.DossierBestandEntity;
import nl.rotterdam.verbonden.core.persistence.DossierInzageEntity;
import nl.rotterdam.verbonden.core.persistence.GetuigeEntity;
import nl.rotterdam.verbonden.core.persistence.HuwelijksDossierEntity;
import nl.rotterdam.verbonden.core.persistence.HuwelijksDossiersPartnerEntity;
import nl.rotterdam.verbonden.core.persistence.MarriageTypeLocationEntity;
import nl.rotterdam.verbonden.core.persistence.TrouwlocatieEntity;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class DossierAdministrationServiceImpl implements DossierAdministrationService {

    private static final int MAX_LENGTE_BESTANDSNAAM = 255;

    private final DossierAdministrationRepository dossierAdministrationRepository;
    private final DossierBestandRepository dossierBestandRepository;
    private final DossierInzageRepository dossierInzageRepository;
    private final GetuigenRepository getuigenRepository;
    private final MarriageTypeLocationRepository marriageTypeLocationRepository;
    private final LocatieRepository locatieRepository;
    private final TrouwboekjeRepository trouwboekjeRepository;
    private final MarriageIntakeService marriageIntakeService;
    private final AfspraakPlanningService afspraakPlanningService;
    private final PersonLookupService personLookupService;
    private final CurrentUserProvider currentUserProvider;
    private final BetalingAdministrationService betalingAdministrationService;
    private final Clock clock;

    DossierAdministrationServiceImpl(DossierAdministrationRepository dossierAdministrationRepository,
                                     DossierBestandRepository dossierBestandRepository,
                                     DossierInzageRepository dossierInzageRepository,
                                     GetuigenRepository getuigenRepository,
                                     MarriageTypeLocationRepository marriageTypeLocationRepository,
                                     LocatieRepository locatieRepository,
                                     TrouwboekjeRepository trouwboekjeRepository,
                                     MarriageIntakeService marriageIntakeService,
                                     AfspraakPlanningService afspraakPlanningService,
                                     PersonLookupService personLookupService,
                                     CurrentUserProvider currentUserProvider,
                                     BetalingAdministrationService betalingAdministrationService,
                                     Clock clock) {
        this.dossierAdministrationRepository = dossierAdministrationRepository;
        this.dossierBestandRepository = dossierBestandRepository;
        this.dossierInzageRepository = dossierInzageRepository;
        this.getuigenRepository = getuigenRepository;
        this.marriageTypeLocationRepository = marriageTypeLocationRepository;
        this.locatieRepository = locatieRepository;
        this.trouwboekjeRepository = trouwboekjeRepository;
        this.marriageIntakeService = marriageIntakeService;
        this.afspraakPlanningService = afspraakPlanningService;
        this.personLookupService = personLookupService;
        this.currentUserProvider = currentUserProvider;
        this.betalingAdministrationService = betalingAdministrationService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListDossierDto> search(String zoekterm, Pageable pageable) {
        return dossierAdministrationRepository.search(zoekterm == null ? "" : zoekterm, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long count(String zoekterm) {
        return dossierAdministrationRepository.countByZoekterm(zoekterm == null ? "" : zoekterm);
    }

    @Override
    @Transactional
    public DossierDetailDto findDetail(UUID dossierId) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        DossierSamenvattingDto samenvatting = marriageIntakeService.findByDossierId(dossierId);

        List<DetailPartnerDto> partners = marriageIntakeService.findPartnerGegevens(dossierId).stream()
                .map(gegevens -> {
                    HuwelijksDossiersPartnerEntity partner = getPartner(dossier, gegevens.volgorde());
                    return new DetailPartnerDto(gegevens,
                            partner.getIdentiteitGecontroleerdDoor(), partner.getIdentiteitGecontroleerdOp());
                })
                .toList();

        ChangeExtrasDto extraKeuzes = new ChangeExtrasDto(
                dossier.isRingenUitwisselen(),
                dossier.isMuziek(),
                dossier.getTrouwboekje() != null ? dossier.getTrouwboekje().getId() : null,
                dossier.isInternationaleAkte());

        registreerInzage(dossierId, InzageActie.DOSSIER_BEKEKEN);

        return new DossierDetailDto(
                dossierId,
                dossier.getStatus(),
                dossier.getKanaal(),
                dossier.getAangemaaktDoor(),
                dossier.getAangemaaktOp(),
                dossier.getIngediendOp(),
                dossier.getRegistratieType(),
                dossier.getCeremonieSoort(),
                samenvatting.huwelijksLocatie(),
                dossier.getVrijeTrouwlocatie() == null ? null : new VrijeTrouwlocatieDto(
                        dossier.getVrijeTrouwlocatie().getNaam(),
                        dossier.getVrijeTrouwlocatie().getAdres(),
                        dossier.getVrijeTrouwlocatie().getTelefoonnummer(),
                        dossier.getVrijeTrouwlocatie().getContactpersoon(),
                        dossier.getVrijeTrouwlocatie().isGoedgekeurd()),
                samenvatting.datumTijdHuwelijk(),
                samenvatting.prijs(),
                partners,
                marriageIntakeService.findGetuigen(dossierId),
                samenvatting.extras(),
                extraKeuzes,
                samenvatting.totalPrijs(),
                samenvatting.compleet(),
                dossierBestandRepository.findOverzichtByDossierId(dossier.getId()),
                betalingAdministrationService.findByDossierId(dossierId),
                betaalmutaties(dossier),
                teVeelBetaald(dossier, samenvatting));
    }

    /**
     * Wat er meer betaald of te betalen is dan het dossier kost. Het dossier kost de totaalprijs plus de handmatige
     * betaalmutaties; automatische mutaties (prijswijziging) zitten al in de totaalprijs.
     */
    private static Bedrag teVeelBetaald(HuwelijksDossierEntity dossier, DossierSamenvattingDto samenvatting) {
        if (!DossierStatus.BETAALMUTATIES_MOGELIJK.contains(dossier.getStatus())) {
            return null;
        }
        List<ListBetaalmutatieDto> mutaties = betaalmutaties(dossier);
        Bedrag betaald = dossier.getBetalingen().stream()
                .filter(betaling -> betaling.getStatus() == BetalingStatus.GELUKT)
                .map(BetalingEntity::getBedrag)
                .reduce(Bedrag.ZERO, Bedrag::plus);
        Bedrag open = som(mutaties.stream().filter(mutatie -> !mutatie.betaald()).toList());
        Bedrag handmatig = som(mutaties.stream().filter(mutatie -> !mutatie.prijswijziging()).toList());
        Bedrag teVeel = betaald.plus(open).minus(totaal(samenvatting).plus(handmatig));
        return teVeel.isPositive() ? teVeel : null;
    }

    private static List<ListBetaalmutatieDto> betaalmutaties(HuwelijksDossierEntity dossier) {
        return dossier.getBetaalmutaties().stream()
                .map(mutatie -> {
                    List<BetalingEntity> betalingen = dossier.getBetalingen().stream()
                            // Op id: getBetaalmutatie() kan een Hibernate-proxy zijn
                            .filter(betaling -> betaling.getBetaalmutatie() != null
                                    && betaling.getBetaalmutatie().getId().equals(mutatie.getId()))
                            .toList();
                    return new ListBetaalmutatieDto(
                            mutatie.getId(),
                            mutatie.getOmschrijving(),
                            mutatie.getBedrag(),
                            betalingen.stream().anyMatch(betaling -> betaling.getStatus() == BetalingStatus.GELUKT),
                            betalingen.isEmpty(),
                            mutatie.isPrijswijziging(),
                            mutatie.getAangemaaktDoor(),
                            mutatie.getAangemaaktOp());
                })
                .toList();
    }

    private static Bedrag som(List<ListBetaalmutatieDto> mutaties) {
        return mutaties.stream().map(ListBetaalmutatieDto::bedrag).reduce(Bedrag.ZERO, Bedrag::plus);
    }

    @Override
    @Transactional
    public long createBetaalmutatie(UUID dossierId, CreateBetaalmutatieDto dto) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        if (!DossierStatus.BETAALMUTATIES_MOGELIJK.contains(dossier.getStatus())) {
            throw new BetaalmutatieNietMogelijkException(
                    "Een betaalmutatie kan alleen bij een ingediend of geaccepteerd dossier.");
        }
        String omschrijving = dto.omschrijving() == null ? "" : dto.omschrijving().strip();
        if (omschrijving.isEmpty() || omschrijving.length() > CreateBetaalmutatieDto.MAX_LENGTE_OMSCHRIJVING) {
            throw new BetaalmutatieNietMogelijkException("Vul een omschrijving in van hoogstens "
                    + CreateBetaalmutatieDto.MAX_LENGTE_OMSCHRIJVING + " tekens.");
        }
        if (dto.bedrag() == null || !dto.bedrag().isPositive()) {
            throw new BetaalmutatieNietMogelijkException("Het bedrag moet groter zijn dan nul.");
        }
        BetaalmutatieEntity mutatie = dossier.voegBetaalmutatieToe(omschrijving, dto.bedrag(), false, medewerker(),
                LocalDateTime.now(clock));
        dossierAdministrationRepository.flush();
        return mutatie.getId();
    }

    @Override
    @Transactional
    public void deleteBetaalmutatie(UUID dossierId, long betaalmutatieId) {
        try {
            getDossier(dossierId).verwijderBetaalmutatie(betaalmutatieId);
        } catch (IllegalStateException e) {
            throw new BetaalmutatieNietMogelijkException(
                    "Deze betaalmutatie kan niet meer weg: de burger heeft er al een betaling voor gestart.");
        }
    }

    @Override
    @Transactional
    public UUID create(CreateBalieDossierDto dto) {
        if (dto.kanaal() == AanmaakKanaal.ONLINE) {
            throw new IllegalArgumentException("Een medewerker maakt een dossier aan via de balie of video, niet online");
        }
        controleerNieuwePartner(dto.bsnPartner1());
        boolean partner2HeeftBsn = dto.bsnPartner2() != null;
        if (partner2HeeftBsn == (dto.persoonsnummerPartner2() != null)) {
            throw new IllegalArgumentException("Partner 2 heeft óf een BSN, óf een buitenlands persoonsnummer");
        }
        if (partner2HeeftBsn) {
            if (dto.bsnPartner2().equals(dto.bsnPartner1())) {
                throw new IllegalArgumentException("De partners moeten verschillende BSN's hebben");
            }
            controleerNieuwePartner(dto.bsnPartner2());
        } else {
            controleerPersoonsgegevens(dto.persoonsgegevensPartner2());
        }

        String medewerker = medewerker();
        HuwelijksDossierEntity dossier = new HuwelijksDossierEntity(dto.kanaal(), medewerker);
        boolean vrijeLocatieIngevuld = !isLeeg(dto.vrijeLocatieNaam()) || !isLeeg(dto.vrijeLocatieAdres())
                || dto.vrijeLocatieTelefoonnummer() != null || !isLeeg(dto.vrijeLocatieContactpersoon());
        if (dto.ceremonieSoort() == CeremonieSoort.GROOT && dto.locatieId() == null) {
            throw new IllegalArgumentException("Kies een trouwlocatie voor een grote ceremonie");
        }
        TrouwlocatieEntity locatie = dto.locatieId() == null
                ? locatieVoor(dto.ceremonieSoort())
                : locatieRepository.findById(dto.locatieId())
                        .orElseThrow(() -> new IllegalArgumentException("Trouwlocatie niet gevonden: " + dto.locatieId()));
        boolean vrijeLocatie = locatie != null && locatie.isVrijeLocatie();
        if (vrijeLocatie && dto.ceremonieSoort() != CeremonieSoort.GROOT) {
            throw new IllegalArgumentException("Een vrije trouwlocatie is alleen mogelijk bij een grote ceremonie");
        }
        if (vrijeLocatie != vrijeLocatieIngevuld) {
            throw new IllegalArgumentException(vrijeLocatie
                    ? "Vul de verplichte gegevens van de vrije trouwlocatie in"
                    : "Vul alleen gegevens van een vrije trouwlocatie in bij de vrije locatie");
        }
        dossier.wijzigCeremonie(dto.registratieType(), dto.ceremonieSoort(), locatie);
        if (vrijeLocatie) {
            dossier.kiesVrijeTrouwlocatie(locatie, dto.vrijeLocatieNaam(), dto.vrijeLocatieAdres(),
                    dto.vrijeLocatieTelefoonnummer(), dto.vrijeLocatieContactpersoon());
        }
        dossier.voegPartnerToe(dto.bsnPartner1(), medewerker);
        if (partner2HeeftBsn) {
            dossier.voegPartnerToe(dto.bsnPartner2(), medewerker);
        } else {
            HuwelijksDossiersPartnerEntity partner2 = dossier.voegPartnerZonderBsnToe(dto.persoonsnummerPartner2(), medewerker);
            neemPersoonsgegevensOver(partner2, dto.persoonsgegevensPartner2());
        }
        return dossierAdministrationRepository.save(dossier).getUuid();
    }

    private void controleerNieuwePartner(BurgerServiceNummer bsn) {
        if (personLookupService.findByBsn(bsn).isEmpty()) {
            throw new PartnerNietInBrpException(bsn);
        }
        if (dossierAdministrationRepository.existsByPartners_BsnAndStatusIn(bsn, DossierStatus.ACTIEF)) {
            throw new PartnerHeeftAlDossierException(bsn);
        }
    }

    private static void controleerPersoonsgegevens(PersoonsgegevensDto persoonsgegevens) {
        if (persoonsgegevens == null
                || isLeeg(persoonsgegevens.achternaam())
                || isLeeg(persoonsgegevens.voornamen())
                || persoonsgegevens.geboortedatum() == null) {
            throw new IllegalArgumentException(
                    "Van een partner zonder BSN zijn ten minste achternaam, voornamen en geboortedatum nodig");
        }
    }

    private static boolean isLeeg(String waarde) {
        return waarde == null || waarde.isBlank();
    }

    private static void neemPersoonsgegevensOver(HuwelijksDossiersPartnerEntity partner, PersoonsgegevensDto gegevens) {
        partner.setAchternaam(gegevens.achternaam());
        partner.setVoornamen(gegevens.voornamen());
        partner.setGeboortedatum(gegevens.geboortedatum());
        partner.setGeboorteplaats(gegevens.geboorteplaats());
        partner.setNationaliteit(gegevens.nationaliteit());
        partner.setBurgerlijkeStaat(gegevens.burgerlijkeStaat());
    }

    /**
     * De locatie die bij de ceremonie hoort; {@code null} wanneer de ceremonie op meerdere locaties kan.
     */
    private TrouwlocatieEntity locatieVoor(CeremonieSoort ceremonieSoort) {
        return marriageTypeLocationRepository.findByMarriageType_Soort(ceremonieSoort)
                .map(MarriageTypeLocationEntity::getLocatie)
                .orElse(null);
    }

    @Override
    @Transactional
    public void updateCeremonie(UUID dossierId, ChangeCeremonieDto dto) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        DossierSamenvattingDto voor = samenvattingVoorMeerprijs(dossier);
        dossier.wijzigCeremonie(dto.registratieType(), dto.ceremonieSoort(), locatieVoor(dto.ceremonieSoort()));
        if (voor != null) {
            voegBetaalmutatieToeVoorMeerprijs(dossier, voor, marriageIntakeService.findByDossierId(dossierId),
                    "Ceremonie gewijzigd naar " + dto.ceremonieSoort().getLabel().toLowerCase());
        }
    }

    /**
     * De samenvatting van vóór een wijziging, om de meerprijs te kunnen bepalen; {@code null} als het dossier geen
     * betaalmutaties kent (bijv. een concept), zodat de samenvatting dan niet voor niets wordt opgehaald.
     */
    private DossierSamenvattingDto samenvattingVoorMeerprijs(HuwelijksDossierEntity dossier) {
        return DossierStatus.BETAALMUTATIES_MOGELIJK.contains(dossier.getStatus())
                ? marriageIntakeService.findByDossierId(dossier.getUuid())
                : null;
    }

    /**
     * Wordt een ingediend dossier door een wijziging duurder, dan moet het paar het verschil nog betalen: dat wordt
     * een betaalmutatie. Wordt het goedkoper, dan valt dat onder crediteren, en dat gaat buiten Verbonden om; de
     * detailpagina waarschuwt de medewerker dan.
     */
    private void voegBetaalmutatieToeVoorMeerprijs(HuwelijksDossierEntity dossier, DossierSamenvattingDto voor,
                                                   DossierSamenvattingDto na, String omschrijving) {
        Bedrag meerprijs = totaal(na).minus(totaal(voor));
        if (meerprijs.isPositive()) {
            int max = CreateBetaalmutatieDto.MAX_LENGTE_OMSCHRIJVING;
            String kort = omschrijving.length() > max ? omschrijving.substring(0, max - 1) + "…" : omschrijving;
            dossier.voegBetaalmutatieToe(kort, meerprijs, true, medewerker(), LocalDateTime.now(clock));
        }
    }

    private static Bedrag totaal(DossierSamenvattingDto samenvatting) {
        return samenvatting.totalPrijs() != null ? samenvatting.totalPrijs() : Bedrag.ZERO;
    }

    /** De extra's die er door een wijziging bij zijn gekomen, voor de omschrijving van de betaalmutatie. */
    private static String nieuweExtras(DossierSamenvattingDto voor, DossierSamenvattingDto na) {
        Set<String> eerder = voor.extras().stream().map(SidebarExtraItemDto::naam).collect(Collectors.toSet());
        String nieuw = na.extras().stream()
                .filter(extra -> extra.prijs() != null && !eerder.contains(extra.naam()))
                .map(SidebarExtraItemDto::naam)
                .collect(Collectors.joining(", "));
        return nieuw.isEmpty() ? "Extra's gewijzigd" : nieuw;
    }

    @Override
    @Transactional
    public void keurVrijeTrouwlocatieGoed(UUID dossierId) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        if (dossier.getVrijeTrouwlocatie() == null) {
            throw new IllegalStateException("Dit dossier heeft geen vrije trouwlocatie");
        }
        dossier.getVrijeTrouwlocatie().keurGoed();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> findBeschikbareTijden(UUID dossierId, LocalDate datum) {
        return afspraakPlanningService.findBeschikbareTijden(dossierId, datum);
    }

    @Override
    @Transactional
    public void updateAfspraak(UUID dossierId, LocalDate datum, LocalTime startTijd) {
        afspraakPlanningService.boekAfspraak(dossierId, datum, startTijd);
    }

    @Override
    @Transactional
    public void updatePartner(UUID dossierId, ChangePartnerDto dto) {
        HuwelijksDossiersPartnerEntity partner = getPartner(getDossier(dossierId), dto.volgorde());
        if (partner.getVersie() != dto.versie()) {
            throw new OptimisticLockingFailureException("Gegevens van partner " + dto.volgorde() + " in dossier "
                    + dossierId + " zijn gewijzigd: versie " + partner.getVersie() + ", verwacht " + dto.versie());
        }
        partner.setGekozenAchternaam(dto.gekozenAchternaam());
        partner.setTelefoonnummer(dto.telefoonnummer());
        partner.setEmailadres(dto.emailadres());

        if (partner.getBsn() != null) {
            if (dto.buitenlandsPersoonsnummer() != null || dto.persoonsgegevens() != null) {
                throw new IllegalArgumentException("De persoonsgegevens van een partner met BSN komen uit de BRP");
            }
        } else {
            if (dto.buitenlandsPersoonsnummer() == null) {
                throw new IllegalArgumentException("Een partner zonder BSN heeft een buitenlands persoonsnummer nodig");
            }
            controleerPersoonsgegevens(dto.persoonsgegevens());
            partner.setBuitenlandsPersoonsnummer(dto.buitenlandsPersoonsnummer());
            neemPersoonsgegevensOver(partner, dto.persoonsgegevens());
        }
        // Flush, zodat Hibernate de versie controleert binnen deze transactie
        dossierAdministrationRepository.flush();
    }

    @Override
    @Transactional
    public void updateGetuigen(UUID dossierId, List<SaveGetuigenDto> getuigen) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
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
    @Transactional(readOnly = true)
    public List<TrouwboekjeKeuzeDto> findTrouwboekjes() {
        return marriageIntakeService.findActieveTrouwboekjes();
    }

    @Override
    @Transactional
    public void updateExtras(UUID dossierId, ChangeExtrasDto dto) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        DossierSamenvattingDto voor = samenvattingVoorMeerprijs(dossier);
        dossier.wijzigExtras(dto.ringenUitwisselen(), dto.muziek(), dto.trouwboekjeId() != null
                ? trouwboekjeRepository.findById(dto.trouwboekjeId()).orElse(null)
                : null, dto.internationaleAkte());

        // Bij een ingediend dossier ligt de prijs van de internationale akte vast op het tarief van de indieningsdatum
        if (dossier.getIngediendOp() != null) {
            dossier.setInternationaleAktePrijs(dossier.isInternationaleAkte()
                    ? InternationaleAkteTarief.prijsOp(dossier.getIngediendOp().toLocalDate())
                    : null);
        }
        if (voor != null) {
            DossierSamenvattingDto na = marriageIntakeService.findByDossierId(dossierId);
            voegBetaalmutatieToeVoorMeerprijs(dossier, voor, na, nieuweExtras(voor, na));
        }
    }

    @Override
    @Transactional
    public void dienIn(UUID dossierId) {
        marriageIntakeService.dienIn(dossierId);
    }

    @Override
    @Transactional
    public void accepteer(UUID dossierId) {
        beoordeel(dossierId, DossierStatus.GEACCEPTEERD);
    }

    @Override
    @Transactional
    public void wijsAf(UUID dossierId) {
        beoordeel(dossierId, DossierStatus.AFGEWEZEN);
    }

    @Override
    @Transactional
    public void annuleer(UUID dossierId) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        if (!dossier.getStatus().isActief()) {
            throw new DossierNietWijzigbaarException(dossierId, dossier.getStatus(), DossierStatus.ACTIEF);
        }
        dossier.setStatus(DossierStatus.GEANNULEERD);
    }

    private void beoordeel(UUID dossierId, DossierStatus nieuweStatus) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        if (dossier.getStatus() != DossierStatus.INGEDIEND) {
            throw new DossierNietWijzigbaarException(dossierId, dossier.getStatus(), DossierStatus.INGEDIEND);
        }
        dossier.setStatus(nieuweStatus);
    }

    @Override
    @Transactional
    public long createBestand(UUID dossierId, CreateDossierBestandDto dto) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        String bestandsnaam = schoonBestandsnaamOp(dto.bestandsnaam());
        DossierBestandType type = DossierBestandType.vanBestandsnaam(bestandsnaam)
                .orElseThrow(() -> new BestandNietToegestaanException("Dit soort bestand is niet toegestaan. Toegestaan zijn: "
                        + String.join(", ", DossierBestandType.alleExtensies()) + "."));
        if (dto.inhoud().length == 0) {
            throw new BestandNietToegestaanException("Het bestand is leeg.");
        }
        if (dto.inhoud().length > DossierBestandType.MAX_GROOTTE) {
            throw new BestandNietToegestaanException("Het bestand is groter dan "
                    + DossierBestandType.MAX_GROOTTE / (1024 * 1024) + " MB.");
        }
        if (!type.heeftGeldigeInhoud(dto.inhoud())) {
            throw new BestandNietToegestaanException(
                    "De inhoud van het bestand past niet bij de extensie; het bestand is mogelijk beschadigd of hernoemd.");
        }
        DossierBestandEntity bestand = new DossierBestandEntity(dossier, bestandsnaam, type, dto.inhoud(), medewerker());
        return dossierBestandRepository.save(bestand).getId();
    }

    /**
     * Alleen de naam zelf, zonder pad (sommige browsers sturen het volledige pad mee) en zonder stuurtekens.
     */
    private static String schoonBestandsnaamOp(String bestandsnaam) {
        String naam = bestandsnaam.substring(Math.max(bestandsnaam.lastIndexOf('/'), bestandsnaam.lastIndexOf('\\')) + 1)
                .replaceAll("\\p{Cntrl}", "")
                .strip();
        if (naam.length() > MAX_LENGTE_BESTANDSNAAM) {
            int punt = naam.lastIndexOf('.');
            String extensie = punt >= 0 ? naam.substring(punt) : "";
            naam = naam.substring(0, MAX_LENGTE_BESTANDSNAAM - extensie.length()) + extensie;
        }
        return naam;
    }

    @Override
    @Transactional
    public DossierBestandDto findBestand(UUID dossierId, long bestandId) {
        DossierBestandEntity bestand = getBestand(dossierId, bestandId);
        registreerInzage(dossierId, InzageActie.BESTAND_GEDOWNLOAD);
        return new DossierBestandDto(bestand.getBestandsnaam(), bestand.getBestandType(), bestand.getInhoud());
    }

    @Override
    @Transactional
    public void deleteBestand(UUID dossierId, long bestandId) {
        dossierBestandRepository.delete(getBestand(dossierId, bestandId));
    }

    private DossierBestandEntity getBestand(UUID dossierId, long bestandId) {
        return dossierBestandRepository.findByIdAndDossier_Id(bestandId, getDossier(dossierId).getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Bestand " + bestandId + " hoort niet bij dossier " + dossierId));
    }

    private void registreerInzage(UUID dossierId, InzageActie actie) {
        dossierInzageRepository.save(new DossierInzageEntity(dossierId, medewerker(), actie));
    }

    private String medewerker() {
        return currentUserProvider.getCurrentUser().getUserId();
    }

    private HuwelijksDossierEntity getDossier(UUID dossierId) {
        return dossierAdministrationRepository.findByUuid(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier niet gevonden: " + dossierId));
    }

    private static HuwelijksDossiersPartnerEntity getPartner(HuwelijksDossierEntity dossier, int volgorde) {
        return dossier.getPartners().stream()
                .filter(p -> p.getVolgorde() == volgorde)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Dossier " + dossier.getUuid() + " heeft geen partner " + volgorde));
    }
}
