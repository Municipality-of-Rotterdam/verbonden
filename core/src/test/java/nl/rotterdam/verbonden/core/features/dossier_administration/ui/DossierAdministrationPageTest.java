package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.visit.IVisit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@WithMockUser(username = "medewerker", roles = "BEHEERDER")
class DossierAdministrationPageTest extends BaseWicketTest {

    @Autowired
    private CompleetDossierTestData testData;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Test
    void ingediendDossier_kanWordenGeaccepteerd() {
        UUID dossierId = testData.maakCompleetDossier();
        marriageIntakeService.dienIn(dossierId);

        startPageGefilterdOp(dossierId);
        tester.assertContains("Ingediend");

        tester.executeAjaxEvent(vindActieButton("accepteerButton"), "click");

        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.GEACCEPTEERD);
        tester.assertContains("Geaccepteerd");
    }

    @Test
    void ingediendDossier_kanWordenAfgewezen() {
        UUID dossierId = testData.maakCompleetDossier();
        marriageIntakeService.dienIn(dossierId);

        startPageGefilterdOp(dossierId);
        tester.executeAjaxEvent(vindActieButton("wijsAfButton"), "click");

        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.AFGEWEZEN);
        tester.assertContains("Afgewezen");
    }

    @Test
    void conceptDossier_toontGeenActies() {
        UUID dossierId = testData.maakConceptDossier();

        startPageGefilterdOp(dossierId);

        tester.assertContains("Concept");
        assertThat(tester.getLastResponseAsString()).doesNotContain("Accepteren").doesNotContain("Afwijzen");
    }

    private void startPageGefilterdOp(UUID dossierId) {
        DossierAdministrationPage page = tester.startPage(DossierAdministrationPage.class);
        page.visitChildren(TextField.class, (TextField<?> veld, IVisit<Void> visit) -> {
            veld.setDefaultModelObject(dossierId.toString());
            visit.stop();
        });
        tester.startPage(page);
    }

    private String vindActieButton(String id) {
        return tester.getLastRenderedPage().visitChildren((Component component, IVisit<String> visit) -> {
            if (component.getId().equals(id) && component.isVisibleInHierarchy()) {
                visit.stop(component.getPageRelativePath());
            }
        });
    }
}
