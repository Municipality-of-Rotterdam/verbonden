package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.huwelijk.integration_test.BaseWicketTest;
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
}
