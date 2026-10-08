package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
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

    private static final BurgerServiceNummer PARTNER_1 = new BurgerServiceNummer("999990007");
    private static final BurgerServiceNummer PARTNER_2 = new BurgerServiceNummer("999990019");

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
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990007")));

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
        tester.executeBehavior(telefoonnummerControl.getBehaviors(AjaxFormComponentUpdatingBehavior.class).getFirst());

        // Trigger auto-save for emailadres via AjaxFormComponentUpdatingBehavior
        FormComponent<?> emailadresControl = (FormComponent<?>) tester.getComponentFromLastRenderedPage(
                formPath + ":emailadresInput:input-container:control");
        tester.getRequest().getPostParameters().setParameterValue(
                emailadresControl.getInputName(), "new@example.com");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.executeBehavior(emailadresControl.getBehaviors(AjaxFormComponentUpdatingBehavior.class).getFirst());

        List<PartnerGegevensDto> partners = marriageIntakeService.findPartnerGegevens(createdDossierId);
        PartnerGegevensDto partner = partners.stream()
                .filter(p -> new BurgerServiceNummer("999990007").equals(p.bsn()))
                .findFirst()
                .orElseThrow();

        assertThat(partner.telefoonnummer()).isEqualTo(new Telefoonnummer("0612345999"));
        assertThat(partner.emailadres()).isEqualTo(new Emailadres("new@example.com"));
    }

    @Test
    @WithMockUser(username = "999990019")
    void partnerKanContactGegevensVanDeAnderWijzigen() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, PARTNER_1));
        marriageIntakeService.acceptInvitation(createdDossierId, PARTNER_2);
        startJullieGegevensPage();

        wijzigTelefoonnummerOpKaart(0, "0612345999");

        assertThat(partner(PARTNER_1).telefoonnummer()).isEqualTo(new Telefoonnummer("0612345999"));
        assertThat(partner(PARTNER_2).telefoonnummer()).isNull();
    }

    @Test
    @WithMockUser(username = "999990019")
    void gelijktijdigeWijzigingDoorPartnerOverschrijftNietMaarToontActueleGegevens() {
        createdDossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, PARTNER_1));
        marriageIntakeService.acceptInvitation(createdDossierId, PARTNER_2);
        startJullieGegevensPage();

        // Partner 1 wijzigt intussen in een eigen sessie het e-mailadres
        marriageIntakeService.slaContactGegevensOp(createdDossierId, 1, partner(PARTNER_1).versie(),
                null, new Emailadres("partner1@example.com"));

        wijzigTelefoonnummerOpKaart(0, "0612345999");

        PartnerGegevensDto partner1 = partner(PARTNER_1);
        assertThat(partner1.telefoonnummer()).isNull();
        assertThat(partner1.emailadres()).isEqualTo(new Emailadres("partner1@example.com"));
        assertThat(tester.getLastResponseAsString())
                .contains("intussen door uw partner gewijzigd")
                .contains("partner1@example.com");

        // Na het verversen kan de wijziging alsnog worden doorgevoerd
        wijzigTelefoonnummerOpKaart(0, "0612345999");

        partner1 = partner(PARTNER_1);
        assertThat(partner1.telefoonnummer()).isEqualTo(new Telefoonnummer("0612345999"));
        assertThat(partner1.emailadres()).isEqualTo(new Emailadres("partner1@example.com"));
    }

    private void startJullieGegevensPage() {
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.startPage(JullieGegevensPage.class, new PageParameters().add("dossierId", createdDossierId.toString()));
        tester.assertRenderedPage(JullieGegevensPage.class);
    }

    private void wijzigTelefoonnummerOpKaart(int kaart, String telefoonnummer) {
        FormComponent<?> control = (FormComponent<?>) tester.getComponentFromLastRenderedPage(
                "pageLayout:pageLayout_body:pageBody:pageBody_body:partnerCards:" + kaart
                        + ":contactGegevensForm:telefoonnummerInput:input-container:control");
        tester.getRequest().getPostParameters().setParameterValue(control.getInputName(), telefoonnummer);
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.executeBehavior(control.getBehaviors(AjaxFormComponentUpdatingBehavior.class).getFirst());
    }

    private PartnerGegevensDto partner(BurgerServiceNummer bsn) {
        return marriageIntakeService.findPartnerGegevens(createdDossierId).stream()
                .filter(p -> bsn.equals(p.bsn()))
                .findFirst()
                .orElseThrow();
    }
}
