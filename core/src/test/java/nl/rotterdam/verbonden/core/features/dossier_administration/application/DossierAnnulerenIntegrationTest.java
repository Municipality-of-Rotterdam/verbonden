package nl.rotterdam.verbonden.core.features.dossier_administration.application;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.CreateBalieDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.AanmaakKanaal;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierAccessOutcome;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData.BSN_PARTNER_1;
import static nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData.BSN_PARTNER_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Een geannuleerd dossier blijft bewaard, maar telt niet meer als het dossier van de partners: zij kunnen daarna
 * opnieuw een aanvraag doen.
 */
@VerbondenIntegrationTest
@Transactional
@WithMockUser(username = "medewerker", roles = "BEHEER")
class DossierAnnulerenIntegrationTest {

    private static final BurgerServiceNummer ANDERE_PARTNER = new BurgerServiceNummer("999990056");

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private CompleetDossierTestData testData;

    @Test
    void annuleer_partnersKunnenOpnieuwEenDossierAanmaken() {
        UUID dossierId = testData.maakCompleetDossier();
        marriageIntakeService.dienIn(dossierId);

        dossierAdministrationService.annuleer(dossierId);

        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.GEANNULEERD);
        assertThat(marriageIntakeService.findDossierIdByBsn(BSN_PARTNER_1)).isEmpty();
        assertThat(marriageIntakeService.resolveAccess(dossierId, BSN_PARTNER_1).scenario())
                .isEqualTo(DossierAccessOutcome.Scenario.NOT_AUTHORIZED);

        UUID nieuwDossier = maakDossier(BSN_PARTNER_1);
        assertThat(marriageIntakeService.findDossierIdByBsn(BSN_PARTNER_1)).contains(nieuwDossier);
    }

    @Test
    void annuleer_geeftTijdslotVrij() {
        UUID dossierId = testData.maakCompleetDossier();
        LocalDateTime tijdslot = marriageIntakeService.findByDossierId(dossierId).datumTijdHuwelijk();
        UUID ander = maakDossier(ANDERE_PARTNER);
        assertThat(marriageIntakeService.findAllBeschikbareSlots(ander)).doesNotContain(tijdslot);

        dossierAdministrationService.annuleer(dossierId);

        assertThat(marriageIntakeService.findAllBeschikbareSlots(ander)).contains(tijdslot);
    }

    @Test
    void annuleer_afgeslotenDossier_wordtGeweigerd() {
        UUID dossierId = testData.maakConceptDossier();
        dossierAdministrationService.annuleer(dossierId);

        assertThatThrownBy(() -> dossierAdministrationService.annuleer(dossierId))
                .isInstanceOf(DossierNietWijzigbaarException.class);
    }

    @Test
    void create_aanDeBalie_naGeannuleerdDossier_isToegestaan() {
        UUID oudDossier = testData.maakCompleetDossier();
        dossierAdministrationService.annuleer(oudDossier);

        UUID nieuwDossier = dossierAdministrationService.create(new CreateBalieDossierDto(AanmaakKanaal.BALIE,
                RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, BSN_PARTNER_1, BSN_PARTNER_2, null, null));

        assertThat(marriageIntakeService.findDossierIdByBsn(BSN_PARTNER_2)).contains(nieuwDossier);
    }

    @Test
    void ingediendDossier_blokkeertUitnodiging() {
        UUID eigenDossier = testData.maakCompleetDossier();
        marriageIntakeService.dienIn(eigenDossier);
        UUID uitnodiging = maakDossier(ANDERE_PARTNER);

        assertThat(marriageIntakeService.resolveAccess(uitnodiging, BSN_PARTNER_1))
                .isEqualTo(new DossierAccessOutcome(DossierAccessOutcome.Scenario.INVITATION_BLOCKED, eigenDossier));
    }

    private UUID maakDossier(BurgerServiceNummer bsn) {
        return marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, null, bsn));
    }
}
