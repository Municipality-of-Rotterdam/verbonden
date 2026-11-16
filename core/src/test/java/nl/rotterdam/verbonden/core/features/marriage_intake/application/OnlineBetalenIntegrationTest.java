package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.dossier_administration.application.DossierAdministrationService;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateBetaalmutatieDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetaalmutatieNietTeBetalenException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingLooptAlException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStandDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.TeBetalenDto;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStartResultaat;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietCompleetException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.integration_test.TestOnlineBetaalService;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import nl.rotterdam.verbonden.core.online_betalen.BetaalSoort;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslag;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagMelding;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagVerwerker;
import nl.rotterdam.verbonden.core.online_betalen.StartBetalingOpdracht;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData.BSN_PARTNER_1;
import static nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData.BSN_PARTNER_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@VerbondenIntegrationTest
@Transactional
class OnlineBetalenIntegrationTest {

    private static final String RETOUR_URL = "https://verbonden.test/mijn-dag/x/betaling-retour";

    @Autowired
    private OnlineBetalenService onlineBetalenService;

    @Autowired
    private BetaalUitslagVerwerker verwerker;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private CompleetDossierTestData testData;

    @Autowired
    private TestOnlineBetaalService betaalprovider;

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @PersistenceContext
    private EntityManager entityManager;

    @AfterEach
    void resetBetaalprovider() {
        betaalprovider.reset();
    }

    @Test
    void gratisDossier_wordtZonderBetalingIngediend() {
        UUID dossierId = testData.maakCompleetDossier();

        BetalingStartResultaat resultaat = onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL);

        assertThat(resultaat).isInstanceOf(BetalingStartResultaat.IngediendZonderBetaling.class);
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.INGEDIEND);
        assertThat(betaalprovider.opdrachten()).isEmpty();
        assertThat(laatsteStatus(dossierId)).isEmpty();
    }

    @Test
    void betaaldDossier_startBetalingVanDeTotaalprijsEnBlijftConcept() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();

        BetalingStartResultaat resultaat = onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_2, RETOUR_URL);

        StartBetalingOpdracht opdracht = betaalprovider.laatsteOpdracht();
        assertThat(resultaat).isEqualTo(new BetalingStartResultaat.NaarBetaalprovider(
                TestOnlineBetaalService.BETAALPAGINA + opdracht.betalingskenmerk()));
        assertThat(opdracht.dossierId()).isEqualTo(dossierId);
        assertThat(opdracht.soort()).isEqualTo(BetaalSoort.REGISTRATIE);
        assertThat(opdracht.bedrag()).isEqualTo(marriageIntakeService.findByDossierId(dossierId).totalPrijs());
        assertThat(opdracht.bedrag().isPositive()).isTrue();
        assertThat(opdracht.registratieType()).isEqualTo(RegistratieType.HUWELIJK);
        assertThat(opdracht.betalerBsn()).isEqualTo(BSN_PARTNER_2);
        assertThat(opdracht.betalerNaam()).isEqualTo("C. Dëhlano");
        assertThat(opdracht.retourUrl()).isEqualTo(RETOUR_URL);
        assertThat(opdracht.betalingskenmerk())
                .matches("[1-9][0-9]{15}")
                .hasSizeLessThanOrEqualTo(StartBetalingOpdracht.MAX_LENGTE_BETALINGSKENMERK);

        assertThat(laatsteStatus(dossierId)).contains(BetalingStatus.GESTART);
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.CONCEPT);
    }

    @Test
    void elkeBetaalpogingKrijgtEenEigenKenmerk() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();

        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL);
        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.MISLUKT));
        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL);

        assertThat(betaalprovider.opdrachten())
                .extracting(StartBetalingOpdracht::betalingskenmerk)
                .doesNotHaveDuplicates()
                .hasSize(2);
    }

    @Test
    void geluktBetaling_dientDossierIn() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL);

        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.GELUKT));

        assertThat(laatsteStatus(dossierId)).contains(BetalingStatus.GELUKT);
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.INGEDIEND);
    }

    @Test
    void mislukteBetaling_dossierBlijftConceptEnKanOpnieuwBetalen() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL);

        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.MISLUKT));

        assertThat(laatsteStatus(dossierId)).contains(BetalingStatus.MISLUKT);
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.CONCEPT);

        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL);
        assertThat(laatsteStatus(dossierId)).contains(BetalingStatus.GESTART);
    }

    @Test
    void dezelfdeUitslagNogmaals_veranderNiets() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL);
        String kenmerk = betaalprovider.laatsteOpdracht().betalingskenmerk();
        verwerker.verwerkUitslag(new BetaalUitslagMelding(kenmerk, null, null, BetaalUitslag.GELUKT));

        verwerker.verwerkUitslag(new BetaalUitslagMelding(kenmerk, null, null, BetaalUitslag.GELUKT));
        verwerker.verwerkUitslag(new BetaalUitslagMelding(kenmerk, null, null, BetaalUitslag.MISLUKT));

        assertThat(laatsteStatus(dossierId)).contains(BetalingStatus.GELUKT);
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.INGEDIEND);
    }

    @Test
    void tweedeBetaling_terwijlDeEersteLoopt_wordtGeweigerd() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL);

        // Bijvoorbeeld de andere partner, of een tweede tabblad
        assertThatThrownBy(() -> onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_2, RETOUR_URL))
                .isInstanceOf(BetalingLooptAlException.class);
        assertThat(betaalprovider.opdrachten()).hasSize(1);
    }

    @Test
    void meldingMetAnderKenmerkBijDeBetaalprovider_wordtGenegeerd() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL);
        String kenmerk = betaalprovider.laatsteOpdracht().betalingskenmerk();

        verwerker.verwerkUitslag(new BetaalUitslagMelding(kenmerk, "ander-mandje", null, BetaalUitslag.GELUKT));

        assertThat(laatsteStatus(dossierId)).contains(BetalingStatus.GESTART);
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.CONCEPT);
    }

    @Test
    void meldingMetAnderBedrag_wordtGenegeerd() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL);
        StartBetalingOpdracht opdracht = betaalprovider.laatsteOpdracht();

        verwerker.verwerkUitslag(new BetaalUitslagMelding(opdracht.betalingskenmerk(),
                "extern-" + opdracht.betalingskenmerk(), Bedrag.of("0.01"), BetaalUitslag.GELUKT));

        assertThat(laatsteStatus(dossierId)).contains(BetalingStatus.GESTART);
    }

    @Test
    void meldingDieBijDeBetalingPast_wordtVerwerkt() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL);
        StartBetalingOpdracht opdracht = betaalprovider.laatsteOpdracht();

        verwerker.verwerkUitslag(new BetaalUitslagMelding(opdracht.betalingskenmerk(),
                "extern-" + opdracht.betalingskenmerk(), opdracht.bedrag(), BetaalUitslag.GELUKT));

        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.INGEDIEND);
    }

    @Test
    void onbekendKenmerk_wordtGenegeerd() {
        verwerker.verwerkUitslag(new BetaalUitslagMelding("ONBEKEND", null, null, BetaalUitslag.GELUKT));
    }

    @Test
    void onvolledigDossier_wordtGeweigerd() {
        UUID dossierId = testData.maakConceptDossier();

        assertThatThrownBy(() -> onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL))
                .isInstanceOf(DossierNietCompleetException.class);
        assertThat(betaalprovider.opdrachten()).isEmpty();
    }

    @Test
    void ingediendDossier_wordtGeweigerd() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        marriageIntakeService.dienIn(dossierId);

        assertThatThrownBy(() -> onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL))
                .isInstanceOf(DossierNietWijzigbaarException.class);
    }

    // --- Betaalmutaties ---

    @Test
    @WithMockUser(username = "medewerker", roles = Roles.MEDEWERKER)
    void betaalmutatie_isTeBetalenTotDeBetalingGelukt() {
        UUID dossierId = ingediendDossier();
        long mutatieId = dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("Trouwboekje Luxe (bijbesteld)", Bedrag.of("25.00")));

        assertThat(onlineBetalenService.findTeBetalen(dossierId))
                .containsExactly(new TeBetalenDto(mutatieId, "Trouwboekje Luxe (bijbesteld)", Bedrag.of("25.00"), false));

        String redirectUrl = onlineBetalenService.startMutatieBetaling(dossierId, mutatieId, BSN_PARTNER_2, RETOUR_URL);

        StartBetalingOpdracht opdracht = betaalprovider.laatsteOpdracht();
        assertThat(redirectUrl).isEqualTo(TestOnlineBetaalService.BETAALPAGINA + opdracht.betalingskenmerk());
        assertThat(opdracht.soort()).isEqualTo(BetaalSoort.MUTATIE);
        assertThat(opdracht.bedrag()).isEqualTo(Bedrag.of("25.00"));
        assertThat(opdracht.betalerBsn()).isEqualTo(BSN_PARTNER_2);
        assertThat(onlineBetalenService.findLaatsteBetaling(dossierId))
                .contains(new BetalingStandDto(BetaalSoort.MUTATIE, BetalingStatus.GESTART));
        assertThat(onlineBetalenService.findTeBetalen(dossierId)).singleElement()
                .extracting(TeBetalenDto::inBehandeling).isEqualTo(true);
        assertThatThrownBy(() -> onlineBetalenService.startMutatieBetaling(dossierId, mutatieId, BSN_PARTNER_1, RETOUR_URL))
                .isInstanceOf(BetalingLooptAlException.class);

        verwerker.verwerkUitslag(new BetaalUitslagMelding(opdracht.betalingskenmerk(), null, null, BetaalUitslag.GELUKT));

        assertThat(onlineBetalenService.findTeBetalen(dossierId)).isEmpty();
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.INGEDIEND);
        assertThatThrownBy(() -> onlineBetalenService.startMutatieBetaling(dossierId, mutatieId, BSN_PARTNER_1, RETOUR_URL))
                .isInstanceOf(BetaalmutatieNietTeBetalenException.class);
    }

    /**
     * In een nieuw request (nieuwe sessie) laadt Hibernate de mutatie van een betaling als proxy. Een vergelijking
     * op objectidentiteit ziet de betaalde mutatie dan als onbetaald.
     */
    @Test
    @WithMockUser(username = "medewerker", roles = Roles.MEDEWERKER)
    void betaaldeMutatie_blijftBetaaldInEenNieuweSessie() {
        UUID dossierId = ingediendDossier();
        long mutatieId = dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("Trouwboekje Luxe (bijbesteld)", Bedrag.of("25.00")));
        onlineBetalenService.startMutatieBetaling(dossierId, mutatieId, BSN_PARTNER_2, RETOUR_URL);
        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.GELUKT));

        nieuweSessie();
        assertThat(onlineBetalenService.findBetaaloverzicht(dossierId).open()).isEqualTo(Bedrag.ZERO);
        nieuweSessie();
        assertThat(onlineBetalenService.findTeBetalen(dossierId)).isEmpty();
        nieuweSessie();
        assertThat(dossierAdministrationService.findDetail(dossierId).betaalmutaties())
                .singleElement().satisfies(mutatie -> assertThat(mutatie.betaald()).isTrue());
        nieuweSessie();
        assertThatThrownBy(() -> onlineBetalenService.startMutatieBetaling(dossierId, mutatieId, BSN_PARTNER_1, RETOUR_URL))
                .isInstanceOf(BetaalmutatieNietTeBetalenException.class);
    }

    private void nieuweSessie() {
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @WithMockUser(username = "medewerker", roles = Roles.MEDEWERKER)
    void mislukteBetalingVanEenMutatie_blijftTeBetalen() {
        UUID dossierId = ingediendDossier();
        long mutatieId = dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("Internationale akte", Bedrag.of("17.80")));
        onlineBetalenService.startMutatieBetaling(dossierId, mutatieId, BSN_PARTNER_1, RETOUR_URL);

        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.MISLUKT));

        assertThat(onlineBetalenService.findTeBetalen(dossierId)).extracting(TeBetalenDto::betaalmutatieId)
                .containsExactly(mutatieId);
        assertThat(onlineBetalenService.findLaatsteBetaling(dossierId))
                .contains(new BetalingStandDto(BetaalSoort.MUTATIE, BetalingStatus.MISLUKT));
    }

    @Test
    void conceptDossier_heeftNietsTeBetalenEnKanGeenMutatieBetalen() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();

        assertThat(onlineBetalenService.findTeBetalen(dossierId)).isEmpty();
        assertThatThrownBy(() -> onlineBetalenService.startMutatieBetaling(dossierId, 1L, BSN_PARTNER_1, RETOUR_URL))
                .isInstanceOf(BetaalmutatieNietTeBetalenException.class);
    }

    /** Een ingediend dossier, betaald via de normale flow. */
    private UUID ingediendDossier() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL);
        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.GELUKT));
        return dossierId;
    }

    private Optional<BetalingStatus> laatsteStatus(UUID dossierId) {
        return onlineBetalenService.findLaatsteBetaling(dossierId).map(BetalingStandDto::status);
    }

    @Test
    void betalerMoetPartnerZijn() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();

        assertThatThrownBy(() -> onlineBetalenService.startBetaling(
                dossierId, new BurgerServiceNummer("999990007"), RETOUR_URL))
                .isInstanceOf(IllegalStateException.class);
        assertThat(betaalprovider.opdrachten()).isEmpty();
    }
}
