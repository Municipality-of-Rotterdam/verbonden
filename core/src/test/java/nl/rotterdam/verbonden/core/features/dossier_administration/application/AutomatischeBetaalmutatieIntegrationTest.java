package nl.rotterdam.verbonden.core.features.dossier_administration.application;

import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangeCeremonieDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangeExtrasDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierDetailDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListBetaalmutatieDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.OnlineBetalenService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetaaloverzichtDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveExtrasDto;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.application.TrouwboekjeAdministrationService;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.CreateTrouwboekjeDto;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.integration_test.InstelbareKlok;
import nl.rotterdam.verbonden.core.integration_test.TestOnlineBetaalService;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslag;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagMelding;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagVerwerker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData.BSN_PARTNER_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Wordt een ingediend dossier door een wijziging van een medewerker duurder, dan krijgt het paar het verschil als
 * betaalmutatie te betalen.
 */
@VerbondenIntegrationTest
@Transactional
@WithMockUser(username = "medewerker", roles = Roles.MEDEWERKER)
class AutomatischeBetaalmutatieIntegrationTest {

    private static final Bedrag AKTE_2026 = Bedrag.of("17.80");

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private OnlineBetalenService onlineBetalenService;

    @Autowired
    private TrouwboekjeAdministrationService trouwboekjeAdministrationService;

    @Autowired
    private BetaalUitslagVerwerker verwerker;

    @Autowired
    private CompleetDossierTestData testData;

    @Autowired
    private TestOnlineBetaalService betaalprovider;

    @Autowired
    private InstelbareKlok klok;

    private long linnen;
    private long luxe;

    @BeforeEach
    void trouwboekjes() {
        klok.zetOp(LocalDate.of(2026, 11, 2));
        linnen = trouwboekjeAdministrationService.create(new CreateTrouwboekjeDto(
                "Linnen trouwboekje", null, "/trouwboekjes/linnen.webp", Bedrag.of("42.50"), null, null));
        luxe = trouwboekjeAdministrationService.create(new CreateTrouwboekjeDto(
                "Luxe trouwboekje", null, "/trouwboekjes/luxe.webp", Bedrag.of("60.00"), null, null));
    }

    @AfterEach
    void opruimen() {
        betaalprovider.reset();
        klok.zetTerug();
    }

    @Test
    void alsnogInternationaleAkte_verschilIsTeBetalen() {
        UUID dossierId = betaaldDossierMet(new SaveExtrasDto(false, false, linnen, false));

        dossierAdministrationService.updateExtras(dossierId, new ChangeExtrasDto(false, false, linnen, true));

        assertThat(betaalmutaties(dossierId))
                .extracting(ListBetaalmutatieDto::omschrijving, ListBetaalmutatieDto::bedrag,
                        ListBetaalmutatieDto::prijswijziging)
                .containsExactly(tuple("Internationale huwelijksakte", AKTE_2026, true));
        assertThat(dossierAdministrationService.findDetail(dossierId).teVeelBetaald()).isNull();
        assertThat(onlineBetalenService.findBetaaloverzicht(dossierId))
                .isEqualTo(new BetaaloverzichtDto(Bedrag.of("42.50"), AKTE_2026, Bedrag.ZERO, Bedrag.ZERO));
        assertThat(onlineBetalenService.findBetaaloverzicht(dossierId).stand())
                .isEqualTo(BetaaloverzichtDto.Stand.OPEN);
    }

    @Test
    void duurderTrouwboekje_alleenHetVerschilIsTeBetalen() {
        UUID dossierId = betaaldDossierMet(new SaveExtrasDto(false, false, linnen, false));

        dossierAdministrationService.updateExtras(dossierId, new ChangeExtrasDto(false, false, luxe, false));

        assertThat(betaalmutaties(dossierId))
                .extracting(ListBetaalmutatieDto::omschrijving, ListBetaalmutatieDto::bedrag)
                .containsExactly(tuple("Luxe trouwboekje", Bedrag.of("17.50")));
        assertThat(dossierAdministrationService.findDetail(dossierId).teVeelBetaald()).isNull();
    }

    @Test
    void goedkoper_geenMutatieMaarWaarschuwing() {
        UUID dossierId = betaaldDossierMet(new SaveExtrasDto(false, false, luxe, false));

        dossierAdministrationService.updateExtras(dossierId, new ChangeExtrasDto(false, false, linnen, false));

        DossierDetailDto detail = dossierAdministrationService.findDetail(dossierId);
        assertThat(detail.betaalmutaties()).isEmpty();
        assertThat(detail.teVeelBetaald()).isEqualTo(Bedrag.of("17.50"));
    }

    @Test
    void zonderPrijsverschil_geenMutatie() {
        UUID dossierId = betaaldDossierMet(new SaveExtrasDto(false, false, linnen, false));

        dossierAdministrationService.updateExtras(dossierId, new ChangeExtrasDto(true, false, linnen, false));

        assertThat(betaalmutaties(dossierId)).isEmpty();
    }

    @Test
    void conceptDossier_geenMutatie() {
        UUID dossierId = testData.maakCompleetDossier();

        dossierAdministrationService.updateExtras(dossierId, new ChangeExtrasDto(false, false, luxe, true));

        assertThat(betaalmutaties(dossierId)).isEmpty();
    }

    @Test
    void grotereCeremonie_verschilIsTeBetalen() {
        UUID dossierId = betaaldDossierMet(new SaveExtrasDto(false, false, linnen, false));
        Bedrag voor = dossierAdministrationService.findDetail(dossierId).totaalPrijs();

        dossierAdministrationService.updateCeremonie(dossierId,
                new ChangeCeremonieDto(RegistratieType.HUWELIJK, CeremonieSoort.MIDDELGROOT));

        Bedrag na = dossierAdministrationService.findDetail(dossierId).totaalPrijs();
        assertThat(na.isPositive()).isTrue();
        assertThat(betaalmutaties(dossierId))
                .extracting(ListBetaalmutatieDto::omschrijving, ListBetaalmutatieDto::bedrag)
                .containsExactly(tuple("Ceremonie gewijzigd naar middelgroot", na.minus(voor)));
    }

    /** Een compleet klein huwelijk met deze extra's, online betaald en dus ingediend. */
    private UUID betaaldDossierMet(SaveExtrasDto extras) {
        UUID dossierId = testData.maakCompleetDossier();
        marriageIntakeService.slaExtrasOp(dossierId, extras);
        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, "https://verbonden.test/retour");
        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.GELUKT));
        return dossierId;
    }

    private List<ListBetaalmutatieDto> betaalmutaties(UUID dossierId) {
        return dossierAdministrationService.findDetail(dossierId).betaalmutaties();
    }
}
