package nl.rotterdam.verbonden.core.features.extra_administration.application;

import nl.rotterdam.verbonden.core.features.extra_administration.domain.CreateExtraDto;
import nl.rotterdam.verbonden.core.features.extra_administration.domain.ExtraType;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@VerbondenIntegrationTest
class ExtraAdministrationServiceIntegrationTest {

    @Autowired
    private ExtraAdministrationService extraAdministrationService;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Test
    void findAll_toontAlleenTrouwboekjes() {
        long aantalTrouwboekjesVooraf = extraAdministrationService.count();

        extraAdministrationService.create(new CreateExtraDto(
                ExtraType.TROUWBOEKJE,
                "Linnen trouwboekje",
                "Met linnen kaft",
                null,
                new BigDecimal("42.50"),
                null,
                null
        ));

        assertThat(extraAdministrationService.findAll())
                .extracting(dto -> dto.naam())
                .contains("Linnen trouwboekje")
                .doesNotContain("Internationale huwelijksakte");
        assertThat(extraAdministrationService.count()).isEqualTo(aantalTrouwboekjesVooraf + 1);
    }

    @Test
    void findById_verbergtInternationaleAkteVoorBeheer() {
        long internationaleAkteId = marriageIntakeService.findActiefExtras(ExtraType.INTERNATIONALE_AKTE).stream()
                .findFirst()
                .orElseThrow()
                .id();

        assertThat(extraAdministrationService.findById(internationaleAkteId)).isEmpty();
    }
}
