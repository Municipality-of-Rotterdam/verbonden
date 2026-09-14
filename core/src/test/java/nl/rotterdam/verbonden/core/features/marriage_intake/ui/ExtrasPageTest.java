package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ExtrasPageTest extends BaseWicketTest {

    private static final String INTERNATIONALE_AKTE_INTRO =
            "Hier komt informatie over internationale aktes. U krijgt deze akte direct mee na de ceremonie.";

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Test
    @WithMockUser(username = "999990007")
    void kleinHuwelijk_toontInternationaleAkteOptie() {
        assertInternationaleAkteZichtbaarVoor(CeremonieSoort.KLEIN, "999990007");
    }

    @Test
    @WithMockUser(username = "999990019")
    void middelgrootHuwelijk_toontInternationaleAkteOptie() {
        assertInternationaleAkteZichtbaarVoor(CeremonieSoort.MIDDELGROOT, "999990019");
    }

    @Test
    @WithMockUser(username = "999990020")
    void grootHuwelijk_toontInternationaleAkteOptie() {
        assertInternationaleAkteZichtbaarVoor(CeremonieSoort.GROOT, "999990020");
    }

    @Test
    @WithMockUser(username = "999990202")
    void geregistreerdPartnerschap_toontGeenInternationaleAkteOptie() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.GEREGISTREERD_PARTNERSCHAP, CeremonieSoort.GROOT, null, "999990202"));

        tester.startPage(ExtrasPage.class, new PageParameters().add("dossierId", dossierId.toString()));

        tester.assertRenderedPage(ExtrasPage.class);
        assertThat(tester.getLastResponseAsString()).doesNotContain(INTERNATIONALE_AKTE_INTRO);
    }

    private void assertInternationaleAkteZichtbaarVoor(CeremonieSoort ceremonieSoort, String bsn) {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, ceremonieSoort, null, bsn));

        tester.startPage(ExtrasPage.class, new PageParameters().add("dossierId", dossierId.toString()));

        tester.assertRenderedPage(ExtrasPage.class);
        assertThat(tester.getLastResponseAsString()).contains(INTERNATIONALE_AKTE_INTRO);
        assertThat(tester.getLastResponseAsString()).contains("Internationale huwelijksakte");
    }
}
