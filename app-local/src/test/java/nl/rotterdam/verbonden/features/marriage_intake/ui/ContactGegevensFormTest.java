package nl.rotterdam.verbonden.features.marriage_intake.ui;

import nl.rotterdam.verbonden.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.features.marriage_intake.domain.Emailadres;
import nl.rotterdam.verbonden.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.verbonden.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.features.marriage_intake.domain.Telefoonnummer;
import nl.rotterdam.verbonden.integration_test.BaseWicketTest;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContactGegevensFormTest extends BaseWicketTest {

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
    void testContactGegevensAutoSavesOnFieldChange() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, "999990007"));

        PageParameters params = new PageParameters();
        params.add("dossierId", createdDossierId.toString());

        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.startPage(JullieGegevensPage.class, params);
        tester.assertRenderedPage(JullieGegevensPage.class);

        String formPath = "pageLayout:pageLayout_body:pageBody:pageBody_body:partnerCards:0:contactGegevensForm";

        // Trigger auto-save for telefoonnummer via AjaxFormComponentUpdatingBehavior
        FormComponent<?> telefoonnummerControl = (FormComponent<?>) tester.getComponentFromLastRenderedPage(
                formPath + ":telefoonnummerInput:input-container:control");
        tester.getRequest().getPostParameters().setParameterValue(
                telefoonnummerControl.getInputName(), "0612345999");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.executeBehavior(telefoonnummerControl.getBehaviors(AjaxFormComponentUpdatingBehavior.class).get(0));

        // Trigger auto-save for emailadres via AjaxFormComponentUpdatingBehavior
        FormComponent<?> emailadresControl = (FormComponent<?>) tester.getComponentFromLastRenderedPage(
                formPath + ":emailadresInput:input-container:control");
        tester.getRequest().getPostParameters().setParameterValue(
                emailadresControl.getInputName(), "new@example.com");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.executeBehavior(emailadresControl.getBehaviors(AjaxFormComponentUpdatingBehavior.class).get(0));

        List<PartnerGegevensDto> partners = marriageIntakeService.findPartnerGegevens(createdDossierId);
        PartnerGegevensDto partner = partners.stream()
                .filter(p -> "999990007".equals(p.bsn()))
                .findFirst()
                .orElseThrow();

        assertThat(partner.telefoonnummer()).isEqualTo(new Telefoonnummer("0612345999"));
        assertThat(partner.emailadres()).isEqualTo(new Emailadres("new@example.com"));
    }
}
