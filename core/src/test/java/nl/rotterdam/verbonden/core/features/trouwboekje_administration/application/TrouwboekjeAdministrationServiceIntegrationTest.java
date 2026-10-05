package nl.rotterdam.verbonden.core.features.trouwboekje_administration.application;

import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.CreateTrouwboekjeDto;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.ListTrouwboekjeDto;
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

    @Test
    void create_voegtTrouwboekjeToe() {
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
                .extracting(ListTrouwboekjeDto::naam)
                .contains("Linnen trouwboekje");
        assertThat(trouwboekjeAdministrationService.count()).isEqualTo(aantalTrouwboekjesVooraf + 1);
    }
}
