package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietCompleetException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierNietWijzigbaarException;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierSamenvattingDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveExtrasDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveGetuigenDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SidebarExtraItemDto;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.integration_test.InstelbareKlok;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

@VerbondenIntegrationTest
@Transactional
class DossierIndienenIntegrationTest {

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private CompleetDossierTestData testData;

    @Autowired
    private InstelbareKlok klok;

    @AfterEach
    void zetKlokTerug() {
        klok.zetTerug();
    }

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

        klok.zetOp(LocalDate.of(2026, 10, 26));
        marriageIntakeService.dienIn(dossierId);

        DossierSamenvattingDto dossier = marriageIntakeService.findByDossierId(dossierId);
        assertThat(dossier.status()).isEqualTo(DossierStatus.INGEDIEND);
        assertThat(dossier.ingediendOp()).isEqualTo(LocalDateTime.of(2026, 10, 26, 12, 0));
    }

    @Test
    void dienIn_metInternationaleAkte_legtPrijsVanDatumVanIndienenVast() {
        klok.zetOp(LocalDate.of(2026, 12, 31));
        UUID dossierId = testData.maakCompleetDossier();
        marriageIntakeService.slaExtrasOp(dossierId, new SaveExtrasDto(false, false, null, true));

        marriageIntakeService.dienIn(dossierId);
        // Na de tariefverhoging van 1 januari 2027 blijft de vastgelegde prijs gelden
        klok.zetOp(LocalDate.of(2027, 1, 1));

        DossierSamenvattingDto dossier = marriageIntakeService.findByDossierId(dossierId);
        BigDecimal tarief = new BigDecimal("17.80");
        assertThat(marriageIntakeService.findInternationaleAktePrijs(dossierId)).isEqualByComparingTo(tarief);
        assertThat(dossier.extras())
                .extracting(SidebarExtraItemDto::naam, SidebarExtraItemDto::prijs)
                .containsExactly(tuple("Internationale huwelijksakte", tarief));
        assertThat(dossier.totalPrijs()).isEqualByComparingTo(dossier.prijs().add(tarief));
    }

    @Test
    void ingediendDossier_kanNietMeerWordenGewijzigd() {
        UUID dossierId = testData.maakCompleetDossier();
        marriageIntakeService.dienIn(dossierId);

        assertThatThrownBy(() -> marriageIntakeService.slaGetuigenOp(dossierId, List.of()))
                .isInstanceOf(DossierNietWijzigbaarException.class);
        assertThatThrownBy(() -> marriageIntakeService.slaExtrasOp(dossierId, new SaveExtrasDto(true, false, null, false)))
                .isInstanceOf(DossierNietWijzigbaarException.class);
        assertThatThrownBy(() -> marriageIntakeService.slaPartnerGegevensOp(dossierId, 1, "Anders"))
                .isInstanceOf(DossierNietWijzigbaarException.class);
        assertThatThrownBy(() -> marriageIntakeService.dienIn(dossierId))
                .isInstanceOf(DossierNietWijzigbaarException.class);

        assertThat(marriageIntakeService.findGetuigen(dossierId)).hasSize(2);
    }
}
