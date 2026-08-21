package nl.rotterdam.verbonden.features.marriage_intake.ui;

import nl.rotterdam.verbonden.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.integration_test.BaseWicketTest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

class JullieGegevensPageTest extends BaseWicketTest {

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    private UUID createdDossierId;

    @AfterEach
    void cleanup() {
        if (createdDossierId != null) {
            marriageIntakeService.delete(createdDossierId);
            createdDossierId = null;
        }
    }

    @Test
    @WithMockUser(username = "999990007")
    void testRender() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, "999990007"));

        PageParameters params = new PageParameters();
        params.add("dossierId", createdDossierId.toString());
        tester.startPage(JullieGegevensPage.class, params);
        tester.assertRenderedPage(JullieGegevensPage.class);
    }

    @Test
    @WithMockUser(username = "999990007")
    void sidebarLinkNaarJullieGegevensWerktVanafGetuigenPagina() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, "999990007"));

        PageParameters params = new PageParameters();
        params.add("dossierId", createdDossierId.toString());
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.startPage(DeGetuigenPage.class, params);

        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.clickLink("pageLayout:pageLayout_body:pageBody:pageBody_body:keuzesSidebar:gegevensStatusIcon:jullieGegevensLink");

        tester.assertRenderedPage(JullieGegevensPage.class);
    }
}
