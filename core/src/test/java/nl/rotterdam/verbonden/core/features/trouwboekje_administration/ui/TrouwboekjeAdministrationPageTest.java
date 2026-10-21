package nl.rotterdam.verbonden.core.features.trouwboekje_administration.ui;

import nl.rotterdam.verbonden.core.features.trouwboekje_administration.application.TrouwboekjeAdministrationService;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.CreateTrouwboekjeDto;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@WithMockUser(username = "medewerker", roles = Roles.BEHEER)
class TrouwboekjeAdministrationPageTest extends BaseWicketTest {

    @Autowired
    private TrouwboekjeAdministrationService trouwboekjeAdministrationService;

    @Test
    void toontIconenVoorBewerkenEnVerwijderen() {
        trouwboekjeAdministrationService.create(new CreateTrouwboekjeDto(
                "Linnen trouwboekje", "Met linnen kaft", null, new BigDecimal("42.50"), null, null));

        tester.startPage(TrouwboekjeAdministrationPage.class);

        assertThat(tester.getLastResponseAsString())
                .contains("aria-label=\"Bewerken\"")
                .contains("aria-label=\"Verwijderen\"")
                .contains("rods-icon-edit")
                .contains("rods-icon-trash")
                .doesNotContain(">Bewerken</a>", ">Verwijderen</a>");
    }
}
