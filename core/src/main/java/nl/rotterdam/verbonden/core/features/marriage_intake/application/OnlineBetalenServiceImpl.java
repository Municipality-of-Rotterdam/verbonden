package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStartResultaat;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetaalmutatieNietTeBetalenException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetaaloverzichtDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingLooptAlException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStandDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.TeBetalenDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietCompleetException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.repository.BetalingRepository;
import nl.rotterdam.verbonden.core.features.marriage_intake.repository.DossierRepository;
import nl.rotterdam.verbonden.core.identity.PersonInfo;
import nl.rotterdam.verbonden.core.identity.PersonLookupService;
import nl.rotterdam.verbonden.core.online_betalen.BetaalSoort;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslag;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagMelding;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagVerwerker;
import nl.rotterdam.verbonden.core.online_betalen.BetalingGestart;
import nl.rotterdam.verbonden.core.online_betalen.OnlineBetaalService;
import nl.rotterdam.verbonden.core.online_betalen.StartBetalingOpdracht;
import nl.rotterdam.verbonden.core.persistence.BetaalmutatieEntity;
import nl.rotterdam.verbonden.core.persistence.BetalingEntity;
import nl.rotterdam.verbonden.core.persistence.HuwelijksDossierEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
class OnlineBetalenServiceImpl implements OnlineBetalenService, BetaalUitslagVerwerker {

    private static final Logger LOG = LoggerFactory.getLogger(OnlineBetalenServiceImpl.class);

    /**
     * Het betalingskenmerk bestaat alleen uit cijfers: niet elke betaalprovider accepteert letters (OBM eist
     * {@code \d{1,50}}). 16 willekeurige cijfers maken een botsing praktisch onmogelijk, ook over omgevingen heen
     * en na het opnieuw opbouwen van een database.
     */
    private static final int KENMERK_LENGTE = 16;

    private final DossierRepository dossierRepository;
    private final BetalingRepository betalingRepository;
    private final MarriageIntakeService marriageIntakeService;
    private final PersonLookupService personLookupService;
    private final OnlineBetaalService onlineBetaalService;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    OnlineBetalenServiceImpl(DossierRepository dossierRepository,
                             BetalingRepository betalingRepository,
                             MarriageIntakeService marriageIntakeService,
                             PersonLookupService personLookupService,
                             OnlineBetaalService onlineBetaalService,
                             Clock clock) {
        this.dossierRepository = dossierRepository;
        this.betalingRepository = betalingRepository;
        this.marriageIntakeService = marriageIntakeService;
        this.personLookupService = personLookupService;
        this.onlineBetaalService = onlineBetaalService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public BetalingStartResultaat startBetaling(UUID dossierId, BurgerServiceNummer betaler, String retourUrl) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        if (dossier.getStatus() != DossierStatus.CONCEPT) {
            throw new DossierNietWijzigbaarException(dossierId, dossier.getStatus(), DossierStatus.CONCEPT);
        }
        controleerPartner(dossier, betaler);
        DossierSamenvattingDto samenvatting = marriageIntakeService.findByDossierId(dossierId);
        if (!samenvatting.compleet()) {
            throw new DossierNietCompleetException(dossierId);
        }

        Bedrag bedrag = samenvatting.totalPrijs();
        if (bedrag == null || !bedrag.isPositive()) {
            marriageIntakeService.dienIn(dossierId);
            return new BetalingStartResultaat.IngediendZonderBetaling();
        }

        boolean loopt = dossier.getBetalingen().stream()
                .anyMatch(b -> b.getSoort() == BetaalSoort.REGISTRATIE && b.getStatus() == BetalingStatus.GESTART);
        if (loopt) {
            throw new BetalingLooptAlException("Voor dossier " + dossierId + " loopt al een betaling");
        }
        BetalingEntity betaling = dossier.startRegistratieBetaling(nieuwKenmerk(), bedrag, betaler,
                LocalDateTime.now(clock));
        return new BetalingStartResultaat.NaarBetaalprovider(start(betaling, retourUrl));
    }

    @Override
    @Transactional
    public String startMutatieBetaling(UUID dossierId, long betaalmutatieId, BurgerServiceNummer betaler,
                                       String retourUrl) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        if (!DossierStatus.BETAALMUTATIES_MOGELIJK.contains(dossier.getStatus())) {
            throw new BetaalmutatieNietTeBetalenException("Dossier " + dossierId + " met status "
                    + dossier.getStatus() + " kent geen betaalmutaties");
        }
        controleerPartner(dossier, betaler);
        BetaalmutatieEntity mutatie = dossier.getBetaalmutaties().stream()
                .filter(m -> m.getId() == betaalmutatieId)
                .findFirst()
                .orElseThrow(() -> new BetaalmutatieNietTeBetalenException(
                        "Betaalmutatie " + betaalmutatieId + " hoort niet (meer) bij dossier " + dossierId));
        if (isBetaald(dossier, mutatie)) {
            throw new BetaalmutatieNietTeBetalenException("Betaalmutatie " + betaalmutatieId + " is al betaald");
        }
        if (isInBehandeling(dossier, mutatie)) {
            throw new BetalingLooptAlException("Voor betaalmutatie " + betaalmutatieId + " loopt al een betaling");
        }
        BetalingEntity betaling = dossier.startMutatieBetaling(mutatie, nieuwKenmerk(), betaler,
                LocalDateTime.now(clock));
        return start(betaling, retourUrl);
    }

    /**
     * Meldt de betaling aan bij de betaalprovider. De betaalpoging staat dan al in de database (flush), zodat een
     * databasefout niet pas na het aanmaken van het winkelmandje optreedt. Lukt het aanmelden niet, dan rolt de
     * transactie terug en blijft er geen betaalpoging achter.
     */
    private String start(BetalingEntity betaling, String retourUrl) {
        dossierRepository.flush();
        HuwelijksDossierEntity dossier = betaling.getDossier();
        BurgerServiceNummer betaler = betaling.getBetalerBsn();
        BetalingGestart gestart = onlineBetaalService.startBetaling(new StartBetalingOpdracht(
                dossier.getUuid(),
                betaling.getSoort(),
                betaling.getBetalingskenmerk(),
                betaling.getBedrag(),
                dossier.getRegistratieType(),
                betaler,
                personLookupService.findByBsn(betaler).map(PersonInfo::officieleNaam).orElse(""),
                retourUrl));
        betaling.setExterneReferentie(gestart.externeReferentie());
        LOG.info("Betaling {} ({}) van {} gestart voor dossier {}, kenmerk bij betaalprovider {}",
                betaling.getBetalingskenmerk(), betaling.getSoort(), betaling.getBedrag(), dossier.getUuid(),
                gestart.externeReferentie());
        return gestart.redirectUrl();
    }

    /** Of de laatste betaalpoging voor de mutatie nog op de uitslag wacht. */
    private static boolean isInBehandeling(HuwelijksDossierEntity dossier, BetaalmutatieEntity mutatie) {
        return laatstePoging(dossier, mutatie) == BetalingStatus.GESTART;
    }

    /** De stand van de laatste betaalpoging voor de mutatie, of {@code null} als er nog geen is. */
    private static BetalingStatus laatstePoging(HuwelijksDossierEntity dossier, BetaalmutatieEntity mutatie) {
        return dossier.getBetalingen().stream()
                .filter(b -> hoortBij(b, mutatie))
                .reduce((eerder, later) -> later)
                .map(BetalingEntity::getStatus)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public BetaaloverzichtDto findBetaaloverzicht(UUID dossierId) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        Bedrag betaald = dossier.getBetalingen().stream()
                .filter(betaling -> betaling.getStatus() == BetalingStatus.GELUKT)
                .map(BetalingEntity::getBedrag)
                .reduce(Bedrag.ZERO, Bedrag::plus);
        Bedrag open = Bedrag.ZERO;
        Bedrag inBehandeling = Bedrag.ZERO;
        Bedrag nietGelukt = Bedrag.ZERO;
        if (DossierStatus.BETAALMUTATIES_MOGELIJK.contains(dossier.getStatus())) {
            for (BetaalmutatieEntity mutatie : dossier.getBetaalmutaties()) {
                if (isBetaald(dossier, mutatie)) {
                    continue;
                }
                open = open.plus(mutatie.getBedrag());
                BetalingStatus laatste = laatstePoging(dossier, mutatie);
                if (laatste == BetalingStatus.GESTART) {
                    inBehandeling = inBehandeling.plus(mutatie.getBedrag());
                } else if (laatste == BetalingStatus.MISLUKT) {
                    nietGelukt = nietGelukt.plus(mutatie.getBedrag());
                }
            }
        }
        return new BetaaloverzichtDto(betaald, open, inBehandeling, nietGelukt);
    }

    private static boolean isBetaald(HuwelijksDossierEntity dossier, BetaalmutatieEntity mutatie) {
        return dossier.getBetalingen().stream()
                .anyMatch(b -> hoortBij(b, mutatie) && b.getStatus() == BetalingStatus.GELUKT);
    }

    /** Op id, niet op objectidentiteit: {@code getBetaalmutatie()} kan een Hibernate-proxy zijn. */
    private static boolean hoortBij(BetalingEntity betaling, BetaalmutatieEntity mutatie) {
        return betaling.getBetaalmutatie() != null && betaling.getBetaalmutatie().getId().equals(mutatie.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeBetalenDto> findTeBetalen(UUID dossierId) {
        HuwelijksDossierEntity dossier = getDossier(dossierId);
        if (!DossierStatus.BETAALMUTATIES_MOGELIJK.contains(dossier.getStatus())) {
            return List.of();
        }
        return dossier.getBetaalmutaties().stream()
                .filter(mutatie -> !isBetaald(dossier, mutatie))
                .map(mutatie -> new TeBetalenDto(mutatie.getId(), mutatie.getOmschrijving(), mutatie.getBedrag(),
                        isInBehandeling(dossier, mutatie)))
                .toList();
    }

    private HuwelijksDossierEntity getDossier(UUID dossierId) {
        return dossierRepository.findByUuid(dossierId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier niet gevonden: " + dossierId));
    }

    private static void controleerPartner(HuwelijksDossierEntity dossier, BurgerServiceNummer betaler) {
        if (dossier.getPartners().stream().noneMatch(partner -> betaler.equals(partner.getBsn()))) {
            throw new IllegalStateException("BSN is geen partner in dossier " + dossier.getUuid());
        }
    }

    private String nieuwKenmerk() {
        // Begint niet met een 0, zodat het kenmerk ook als getal gelezen dezelfde lengte houdt
        StringBuilder kenmerk = new StringBuilder().append(1 + random.nextInt(9));
        while (kenmerk.length() < KENMERK_LENGTE) {
            kenmerk.append(random.nextInt(10));
        }
        return kenmerk.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BetalingStandDto> findLaatsteBetaling(UUID dossierId) {
        return betalingRepository.findFirstByDossier_UuidOrderByIdDesc(dossierId)
                .map(betaling -> new BetalingStandDto(betaling.getSoort(), betaling.getStatus()));
    }

    @Override
    @Transactional
    public void verwerkUitslag(BetaalUitslagMelding melding) {
        String betalingskenmerk = melding.betalingskenmerk();
        BetaalUitslag uitslag = melding.uitslag();
        Optional<BetalingEntity> gevonden = betalingRepository.findByBetalingskenmerk(betalingskenmerk);
        if (gevonden.isEmpty()) {
            // Mogelijk is er betaald voor een betaling die Verbonden niet kent: handwerk
            LOG.error("Uitslag {} voor onbekend betalingskenmerk {} genegeerd: handmatig uitzoeken",
                    uitslag, betalingskenmerk);
            return;
        }
        BetalingEntity betaling = gevonden.get();
        if (!pastBij(betaling, melding)) {
            return;
        }
        BetalingStatus nieuweStatus = uitslag == BetaalUitslag.GELUKT ? BetalingStatus.GELUKT : BetalingStatus.MISLUKT;

        if (betaling.getStatus() != BetalingStatus.GESTART) {
            if (betaling.getStatus() != nieuweStatus) {
                LOG.error("Betaling {} was al {}, nu gemeld als {}: handmatig uitzoeken",
                        betalingskenmerk, betaling.getStatus(), nieuweStatus);
            }
            return;
        }

        betaling.rondAf(nieuweStatus, LocalDateTime.now(clock));
        UUID dossierId = betaling.getDossier().getUuid();
        LOG.info("Betaling {} voor dossier {} is {}", betalingskenmerk, dossierId, nieuweStatus);

        if (nieuweStatus == BetalingStatus.GELUKT && betaling.getSoort() == BetaalSoort.REGISTRATIE) {
            dienInNaBetaling(betaling, dossierId);
        }
    }

    /**
     * Of de melding bij de betaling past. Wat de betaalprovider meestuurt aan extra gegevens, moet kloppen; zo kan
     * alleen het kenmerk kennen niet genoeg zijn om een betaling als gelukt te laten registreren.
     */
    private static boolean pastBij(BetalingEntity betaling, BetaalUitslagMelding melding) {
        if (melding.externeReferentie() != null && betaling.getExterneReferentie() != null
                && !melding.externeReferentie().equals(betaling.getExterneReferentie())) {
            LOG.error("Melding voor betaling {} genegeerd: kenmerk bij de betaalprovider is {}, verwacht {}",
                    betaling.getBetalingskenmerk(), melding.externeReferentie(), betaling.getExterneReferentie());
            return false;
        }
        if (melding.uitslag() == BetaalUitslag.GELUKT && melding.bedrag() != null
                && !melding.bedrag().equals(betaling.getBedrag())) {
            LOG.error("Melding voor betaling {} genegeerd: betaald {}, gevraagd {}",
                    betaling.getBetalingskenmerk(), melding.bedrag(), betaling.getBedrag());
            return false;
        }
        return true;
    }

    /**
     * Dient het dossier in na een gelukte betaling. Lukt dat niet, dan blijft de betaling wel vastgelegd: het geld
     * is binnen, dus de backoffice moet het oplossen.
     */
    private void dienInNaBetaling(BetalingEntity betaling, UUID dossierId) {
        HuwelijksDossierEntity dossier = betaling.getDossier();
        if (dossier.getStatus() != DossierStatus.CONCEPT) {
            LOG.error("Betaling {} gelukt, maar dossier {} was al {}: mogelijk dubbel betaald",
                    betaling.getBetalingskenmerk(), dossierId, dossier.getStatus());
            return;
        }
        DossierSamenvattingDto samenvatting = marriageIntakeService.findByDossierId(dossierId);
        if (!samenvatting.compleet()) {
            LOG.error("Betaling {} gelukt, maar dossier {} is niet meer compleet en dus niet ingediend",
                    betaling.getBetalingskenmerk(), dossierId);
            return;
        }
        if (!betaling.getBedrag().equals(samenvatting.totalPrijs())) {
            LOG.warn("Betaling {}: betaald {}, maar de totaalprijs van dossier {} is nu {}",
                    betaling.getBetalingskenmerk(), betaling.getBedrag(), dossierId, samenvatting.totalPrijs());
        }
        marriageIntakeService.dienIn(dossierId);
    }
}
