package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.huwelijk.integration_test.BaseWicketTest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContactGegevensFormTest extends BaseWicketTest {

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Test
    @WithMockUser(username = "999990007")
    void testContactGegevensFormSavesNewValues() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, "999990007"));

        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        tester.startPage(JullieGegevensPage.class, params);
        tester.assertRenderedPage(JullieGegevensPage.class);

        String formPath = "pageLayout:pageLayout_body:pageBody:pageBody_body:partnerCards:0:contactGegevensForm";
        FormTester formTester = tester.newFormTester(formPath);
        formTester.setValue("telefoonnummerInput:input-container:control", "0612345999");
        formTester.setValue("emailadresInput:input-container:control", "new@example.com");

        // Wicket's ResourceIsolationRequestCycleListener requires these Fetch Metadata headers
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");

        formTester.submit();

        tester.assertRenderedPage(JullieGegevensPage.class);

        List<PartnerGegevensDto> partners = marriageIntakeService.findPartnerGegevens(dossierId);
        PartnerGegevensDto partner = partners.stream()
                .filter(p -> "999990007".equals(p.bsn()))
                .findFirst()
                .orElseThrow();

        assertThat(partner.telefoonnummer()).isEqualTo("0612345999");
        assertThat(partner.emailadres()).isEqualTo("new@example.com");
    }
}
