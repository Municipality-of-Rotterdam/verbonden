package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveExtrasDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SidebarExtraItemDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.TrouwboekjeKeuzeDto;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.application.TrouwboekjeAdministrationService;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.CreateTrouwboekjeDto;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@VerbondenIntegrationTest
@Transactional
class ExtrasSelectieIntegrationTest {

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private TrouwboekjeAdministrationService trouwboekjeAdministrationService;

    @Test
    void huwelijk_slaatInternationaleAkteOp() {
        UUID dossierId = maakDossier(RegistratieType.HUWELIJK, "999990007");

        marriageIntakeService.slaExtrasOp(dossierId, new SaveExtrasDto(false, false, null, true));

        assertThat(marriageIntakeService.findExtrasSelecties(dossierId).internationaleAkte()).isTrue();
        assertThat(marriageIntakeService.findByDossierId(dossierId).extras())
                .extracting(SidebarExtraItemDto::naam)
                .containsExactly("Internationale huwelijksakte");
    }

    @Test
    void geregistreerdPartnerschap_negeertInternationaleAkte() {
        UUID dossierId = maakDossier(RegistratieType.GEREGISTREERD_PARTNERSCHAP, "999990202");

        marriageIntakeService.slaExtrasOp(dossierId, new SaveExtrasDto(false, false, null, true));

        assertThat(marriageIntakeService.findExtrasSelecties(dossierId).internationaleAkte()).isFalse();
    }

    @Test
    void gekozenTrouwboekje_wordtOpgeslagenEnMetPrijsInSidebarGetoond() {
        long trouwboekjeId = trouwboekjeAdministrationService.create(new CreateTrouwboekjeDto(
                "Linnen trouwboekje", null, "/trouwboekjes/linnen.webp", new BigDecimal("42.50"), null, null));
        UUID dossierId = maakDossier(RegistratieType.HUWELIJK, "999990019");

        assertThat(marriageIntakeService.findActieveTrouwboekjes())
                .extracting(TrouwboekjeKeuzeDto::id, TrouwboekjeKeuzeDto::afbeelding)
                .contains(tuple(trouwboekjeId, "/trouwboekjes/linnen.webp"));

        marriageIntakeService.slaExtrasOp(dossierId, new SaveExtrasDto(false, false, trouwboekjeId, false));

        assertThat(marriageIntakeService.findExtrasSelecties(dossierId).trouwboekjeId()).isEqualTo(trouwboekjeId);
        assertThat(marriageIntakeService.findByDossierId(dossierId).extras())
                .containsExactly(new SidebarExtraItemDto("Linnen trouwboekje", new BigDecimal("42.50")));
    }

    private UUID maakDossier(RegistratieType registratieType, String bsn) {
        return marriageIntakeService.create(
                new CreateDossierDto(registratieType, CeremonieSoort.KLEIN, null, new BurgerServiceNummer(bsn)));
    }
}
