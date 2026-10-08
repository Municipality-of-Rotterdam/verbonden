package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietCompleetException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveExtrasDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveGetuigenDto;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData.BSN_PARTNER_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@VerbondenIntegrationTest
@Transactional
class DossierIndienenIntegrationTest {

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private CompleetDossierTestData testData;

    @Test
    void nieuwDossier_isConceptEnNietCompleet() {
        UUID dossierId = testData.maakConceptDossier();

        DossierSamenvattingDto dossier = marriageIntakeService.findByDossierId(dossierId);

        assertThat(dossier.status()).isEqualTo(DossierStatus.CONCEPT);
        assertThat(dossier.ingediendOp()).isNull();
        assertThat(dossier.compleet()).isFalse();
    }

    @Test
    void dienIn_onvolledigDossier_wordtGeweigerd() {
        UUID dossierId = testData.maakConceptDossier();

        assertThatThrownBy(() -> marriageIntakeService.dienIn(dossierId))
                .isInstanceOf(DossierNietCompleetException.class);
        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.CONCEPT);
    }

    @Test
    void dienIn_ontbrekendeGetuige_wordtGeweigerd() {
        UUID dossierId = testData.maakCompleetDossier();
        marriageIntakeService.slaGetuigenOp(dossierId, List.of(new SaveGetuigenDto(1, "Kwik van Willegenburgh")));

        assertThat(marriageIntakeService.findByDossierId(dossierId).compleet()).isFalse();
        assertThatThrownBy(() -> marriageIntakeService.dienIn(dossierId))
                .isInstanceOf(DossierNietCompleetException.class);
    }

    @Test
    void dienIn_compleetDossier_krijgtStatusIngediend() {
        UUID dossierId = testData.maakCompleetDossier();
        assertThat(marriageIntakeService.findByDossierId(dossierId).compleet()).isTrue();

        LocalDateTime voorIndienen = LocalDateTime.now();
        marriageIntakeService.dienIn(dossierId);

        DossierSamenvattingDto dossier = marriageIntakeService.findByDossierId(dossierId);
        assertThat(dossier.status()).isEqualTo(DossierStatus.INGEDIEND);
        assertThat(dossier.ingediendOp()).isAfterOrEqualTo(voorIndienen);
    }

    @Test
    void ingediendDossier_kanNietMeerWordenGewijzigd() {
        UUID dossierId = testData.maakCompleetDossier();
        marriageIntakeService.dienIn(dossierId);

        assertThatThrownBy(() -> marriageIntakeService.slaGetuigenOp(dossierId, List.of()))
                .isInstanceOf(DossierNietWijzigbaarException.class);
        assertThatThrownBy(() -> marriageIntakeService.slaExtrasOp(dossierId, new SaveExtrasDto(true, false, null, false)))
                .isInstanceOf(DossierNietWijzigbaarException.class);
        assertThatThrownBy(() -> marriageIntakeService.slaPartnerGegevensOp(dossierId, BSN_PARTNER_1, "Anders"))
                .isInstanceOf(DossierNietWijzigbaarException.class);
        assertThatThrownBy(() -> marriageIntakeService.dienIn(dossierId))
                .isInstanceOf(DossierNietWijzigbaarException.class);

        assertThat(marriageIntakeService.findGetuigen(dossierId)).hasSize(2);
    }
}
