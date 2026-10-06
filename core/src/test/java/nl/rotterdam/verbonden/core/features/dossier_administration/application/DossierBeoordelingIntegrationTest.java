package nl.rotterdam.verbonden.core.features.dossier_administration.application;

import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@VerbondenIntegrationTest
@Transactional
class DossierBeoordelingIntegrationTest {

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private CompleetDossierTestData testData;

    @Test
    void accepteer_ingediendDossier_wordtGeaccepteerd() {
        UUID dossierId = ingediendDossier();

        dossierAdministrationService.accepteer(dossierId);

        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.GEACCEPTEERD);
    }

    @Test
    void wijsAf_ingediendDossier_wordtAfgewezen() {
        UUID dossierId = ingediendDossier();

        dossierAdministrationService.wijsAf(dossierId);

        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.AFGEWEZEN);
    }

    @Test
    void conceptDossier_kanNietWordenBeoordeeld() {
        UUID dossierId = testData.maakConceptDossier();

        assertThatThrownBy(() -> dossierAdministrationService.accepteer(dossierId))
                .isInstanceOf(DossierNietWijzigbaarException.class);
        assertThatThrownBy(() -> dossierAdministrationService.wijsAf(dossierId))
                .isInstanceOf(DossierNietWijzigbaarException.class);
    }

    @Test
    void beoordeeldDossier_kanNietOpnieuwWordenBeoordeeld() {
        UUID dossierId = ingediendDossier();
        dossierAdministrationService.accepteer(dossierId);

        assertThatThrownBy(() -> dossierAdministrationService.wijsAf(dossierId))
                .isInstanceOf(DossierNietWijzigbaarException.class);
    }

    @Test
    void search_toontStatusEnIndieningsmoment() {
        UUID dossierId = ingediendDossier();

        assertThat(dossierAdministrationService.search(dossierId.toString(), PageRequest.of(0, 10)))
                .singleElement()
                .satisfies(dto -> {
                    assertThat(dto.status()).isEqualTo(DossierStatus.INGEDIEND);
                    assertThat(dto.ingediendOp()).isNotNull();
                });
    }

    private UUID ingediendDossier() {
        UUID dossierId = testData.maakCompleetDossier();
        marriageIntakeService.dienIn(dossierId);
        return dossierId;
    }
}
