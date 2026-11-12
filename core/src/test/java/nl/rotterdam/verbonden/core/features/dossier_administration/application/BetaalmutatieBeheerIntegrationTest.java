package nl.rotterdam.verbonden.core.features.dossier_administration.application;

import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.features.betaling_administration.domain.ListBetalingDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.BetaalmutatieNietMogelijkException;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateBetaalmutatieDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierDetailDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ListBetaalmutatieDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.OnlineBetalenService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStatus;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.integration_test.InstelbareKlok;
import nl.rotterdam.verbonden.core.integration_test.TestOnlineBetaalService;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import nl.rotterdam.verbonden.core.online_betalen.BetaalSoort;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslag;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagMelding;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagVerwerker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData.BSN_PARTNER_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@VerbondenIntegrationTest
@Transactional
@WithMockUser(username = "medewerker", roles = Roles.MEDEWERKER)
class BetaalmutatieBeheerIntegrationTest {

    private static final String RETOUR_URL = "https://verbonden.test/retour";

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @Autowired
    private OnlineBetalenService onlineBetalenService;

    @Autowired
    private BetaalUitslagVerwerker verwerker;

    @Autowired
    private CompleetDossierTestData testData;

    @Autowired
    private TestOnlineBetaalService betaalprovider;

    @Autowired
    private InstelbareKlok klok;

    @AfterEach
    void opruimen() {
        betaalprovider.reset();
        klok.zetTerug();
    }

    @Test
    void detail_toontBetalingenEnBetaalmutaties() {
        UUID dossierId = ingediendDossier();
        klok.zetOp(LocalDate.of(2026, 11, 2));
        long mutatieId = dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("  Trouwboekje Luxe (bijbesteld) ", Bedrag.of("25.00")));

        DossierDetailDto detail = dossierAdministrationService.findDetail(dossierId);

        assertThat(detail.betalingen()).singleElement().satisfies(betaling -> {
            assertThat(betaling.soort()).isEqualTo(BetaalSoort.REGISTRATIE);
            assertThat(betaling.waarvoor()).isEqualTo("Aanvraag");
            assertThat(betaling.status()).isEqualTo(BetalingStatus.GELUKT);
            assertThat(betaling.betalerBsn()).isEqualTo(BSN_PARTNER_1);
            assertThat(betaling.afgerondOp()).isNotNull();
        });
        assertThat(detail.betaalmutaties()).containsExactly(new ListBetaalmutatieDto(mutatieId,
                "Trouwboekje Luxe (bijbesteld)", Bedrag.of("25.00"), false, true, false, "medewerker",
                LocalDateTime.of(2026, 11, 2, 12, 0)));
    }

    /** Een handmatige mutatie komt bovenop de totaalprijs; dat is niet te veel betaald. */
    @Test
    void handmatigeMutatie_geenWaarschuwingTeVeelBetaald() {
        UUID dossierId = ingediendDossier();
        long mutatieId = dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("Trouwboekje Luxe (bijbesteld)", Bedrag.of("25.00")));
        assertThat(dossierAdministrationService.findDetail(dossierId).teVeelBetaald()).isNull();

        onlineBetalenService.startMutatieBetaling(dossierId, mutatieId, BSN_PARTNER_1, RETOUR_URL);
        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.GELUKT));

        assertThat(dossierAdministrationService.findDetail(dossierId).teVeelBetaald()).isNull();
    }

    @Test
    void betaaldeMutatie_isBetaaldEnNietMeerVerwijderbaar() {
        UUID dossierId = ingediendDossier();
        long mutatieId = dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("Internationale akte", Bedrag.of("17.80")));
        onlineBetalenService.startMutatieBetaling(dossierId, mutatieId, BSN_PARTNER_1, RETOUR_URL);
        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.GELUKT));

        DossierDetailDto detail = dossierAdministrationService.findDetail(dossierId);

        assertThat(detail.betaalmutaties()).singleElement().satisfies(mutatie -> {
            assertThat(mutatie.betaald()).isTrue();
            assertThat(mutatie.verwijderbaar()).isFalse();
        });
        assertThat(detail.betalingen()).extracting(ListBetalingDto::waarvoor)
                .containsExactly("Aanvraag", "Internationale akte");
        assertThatThrownBy(() -> dossierAdministrationService.deleteBetaalmutatie(dossierId, mutatieId))
                .isInstanceOf(BetaalmutatieNietMogelijkException.class);
    }

    @Test
    void mutatieZonderBetaling_magWeg() {
        UUID dossierId = ingediendDossier();
        long mutatieId = dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("Vergissing", Bedrag.of("10.00")));

        dossierAdministrationService.deleteBetaalmutatie(dossierId, mutatieId);

        assertThat(dossierAdministrationService.findDetail(dossierId).betaalmutaties()).isEmpty();
        assertThat(onlineBetalenService.findTeBetalen(dossierId)).isEmpty();
    }

    @Test
    void conceptDossier_kentGeenBetaalmutaties() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();

        assertThatThrownBy(() -> dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("Trouwboekje", Bedrag.of("25.00"))))
                .isInstanceOf(BetaalmutatieNietMogelijkException.class);
    }

    @Test
    void ongeldigeMutatie_wordtGeweigerd() {
        UUID dossierId = ingediendDossier();

        assertThatThrownBy(() -> dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("Gratis", Bedrag.ZERO)))
                .isInstanceOf(BetaalmutatieNietMogelijkException.class);
        assertThatThrownBy(() -> dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("  ", Bedrag.of("5.00"))))
                .isInstanceOf(BetaalmutatieNietMogelijkException.class);
        assertThatThrownBy(() -> dossierAdministrationService.createBetaalmutatie(dossierId,
                new CreateBetaalmutatieDto("x".repeat(101), Bedrag.of("5.00"))))
                .isInstanceOf(BetaalmutatieNietMogelijkException.class);
    }

    private UUID ingediendDossier() {
        UUID dossierId = testData.maakCompleetBetaaldDossier();
        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, RETOUR_URL);
        verwerker.verwerkUitslag(new BetaalUitslagMelding(betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.GELUKT));
        return dossierId;
    }
}
