package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
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
class DossierUitnodigingPageTest extends BaseWicketTest {

    private static final String PARTNER_2 = "999990093";

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Test
    @WithMockUser(username = PARTNER_2)
    void dossierlinkToontUitnodigingZonderTeKoppelen() {
        UUID dossierId = maakDossierVanPartner1();

        tester.startPage(MarriageIntakePage.class, dossierParameters(dossierId));

        tester.assertRenderedPage(DossierUitnodigingPage.class);
        tester.assertContains("Ja, ik doe mee");
        assertThat(marriageIntakeService.findDossierIdByBsn(new BurgerServiceNummer(PARTNER_2))).isEmpty();
    }

    @Test
    @WithMockUser(username = PARTNER_2)
    void bevestigenKoppeltEnToontDossier() {
        UUID dossierId = maakDossierVanPartner1();
        tester.startPage(DossierUitnodigingPage.class, dossierParameters(dossierId));

        // Zoals een browser: anders weigert Wicket's CSRF-bescherming de POST.
        tester.getRequest().setHeader("Origin", "http://localhost");
        tester.newFormTester("pageLayout:pageLayout_body:pageBody:pageBody_body:form")
                .submit("accepteerButton");

        assertThat(marriageIntakeService.findDossierIdByBsn(new BurgerServiceNummer(PARTNER_2))).contains(dossierId);
        tester.assertRenderedPage(MarriageIntakePage.class);
    }

    private UUID maakDossierVanPartner1() {
        return marriageIntakeService.create(new CreateDossierDto(
                RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, null, new BurgerServiceNummer("999990111")));
    }

    private static PageParameters dossierParameters(UUID dossierId) {
        return new PageParameters().add("dossierId", dossierId.toString());
    }
}
