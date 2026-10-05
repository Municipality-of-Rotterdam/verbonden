package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierAccessOutcome;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierAccessOutcome.Scenario.GRANTED;
import static nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierAccessOutcome.Scenario.INVITED;
import static nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierAccessOutcome.Scenario.NOT_AUTHORIZED;
import static nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierAccessOutcome.Scenario.SWITCHED_DOSSIER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Een dossierlink openen koppelt niemand: pas {@link MarriageIntakeService#acceptInvitation} maakt
 * een burger tweede partner.
 */
@VerbondenIntegrationTest
@Transactional
class DossierUitnodigingIntegrationTest {

    private static final BurgerServiceNummer PARTNER_1 = new BurgerServiceNummer("999990056");
    private static final BurgerServiceNummer PARTNER_2 = new BurgerServiceNummer("999990068");
    private static final BurgerServiceNummer DERDE = new BurgerServiceNummer("999990081");

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Test
    void linkOpenenKoppeltNiet() {
        UUID dossierId = maakDossier(PARTNER_1);

        DossierAccessOutcome outcome = marriageIntakeService.resolveAccess(dossierId, PARTNER_2);

        assertThat(outcome).isEqualTo(new DossierAccessOutcome(INVITED, dossierId));
        assertThat(marriageIntakeService.findDossierIdByBsn(PARTNER_2)).isEmpty();
    }

    @Test
    void accepterenKoppeltAlsTweedePartner() {
        UUID dossierId = maakDossier(PARTNER_1);

        marriageIntakeService.acceptInvitation(dossierId, PARTNER_2);

        assertThat(marriageIntakeService.findDossierIdByBsn(PARTNER_2)).contains(dossierId);
        assertThat(marriageIntakeService.resolveAccess(dossierId, PARTNER_2).scenario()).isEqualTo(GRANTED);
    }

    @Test
    void volDossierWeigertDerde() {
        UUID dossierId = maakDossier(PARTNER_1);
        marriageIntakeService.acceptInvitation(dossierId, PARTNER_2);

        assertThat(marriageIntakeService.resolveAccess(dossierId, DERDE).scenario()).isEqualTo(NOT_AUTHORIZED);
        assertThatThrownBy(() -> marriageIntakeService.acceptInvitation(dossierId, DERDE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void burgerMetEigenDossierKanNietAccepteren() {
        UUID dossierId = maakDossier(PARTNER_1);
        UUID eigenDossier = maakDossier(DERDE);

        assertThat(marriageIntakeService.resolveAccess(dossierId, DERDE))
                .isEqualTo(new DossierAccessOutcome(SWITCHED_DOSSIER, eigenDossier));
        assertThatThrownBy(() -> marriageIntakeService.acceptInvitation(dossierId, DERDE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onbekendDossierIsNietToegestaan() {
        assertThat(marriageIntakeService.resolveAccess(UUID.randomUUID(), PARTNER_2).scenario())
                .isEqualTo(NOT_AUTHORIZED);
    }

    private UUID maakDossier(BurgerServiceNummer bsn) {
        return marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, null, bsn));
    }
}
