package nl.rotterdam.verbonden.core.features.trouwboekje_administration.application;

import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.CreateTrouwboekjeDto;
import nl.rotterdam.verbonden.core.features.extra.domain.ExtraType;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@VerbondenIntegrationTest
class TrouwboekjeAdministrationServiceIntegrationTest {

    @Autowired
    private TrouwboekjeAdministrationService trouwboekjeAdministrationService;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Test
    void findAll_toontAlleenTrouwboekjes() {
        long aantalTrouwboekjesVooraf = trouwboekjeAdministrationService.count();

        trouwboekjeAdministrationService.create(new CreateTrouwboekjeDto(
                "Linnen trouwboekje",
                "Met linnen kaft",
                null,
                new BigDecimal("42.50"),
                null,
                null
        ));

        assertThat(trouwboekjeAdministrationService.findAll())
                .extracting(dto -> dto.naam())
                .contains("Linnen trouwboekje")
                .doesNotContain("Internationale huwelijksakte");
        assertThat(trouwboekjeAdministrationService.count()).isEqualTo(aantalTrouwboekjesVooraf + 1);
    }

    @Test
    void findById_verbergtInternationaleAkteVoorBeheer() {
        long internationaleAkteId = marriageIntakeService.findActiefExtras(ExtraType.INTERNATIONALE_AKTE).stream()
                .findFirst()
                .orElseThrow()
                .id();

        assertThat(trouwboekjeAdministrationService.findById(internationaleAkteId)).isEmpty();
    }
}
