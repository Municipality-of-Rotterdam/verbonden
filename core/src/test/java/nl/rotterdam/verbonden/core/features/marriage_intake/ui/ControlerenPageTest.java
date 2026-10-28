package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import org.apache.wicket.Component;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ControlerenPageTest extends BaseWicketTest {

    private static final String PAGE_BODY = "pageLayout:pageLayout_body:pageBody:pageBody_body";

    @Autowired
    private CompleetDossierTestData testData;

    @Test
    @WithMockUser(username = "999990020")
    void vorigeStapNavigeertNaarExtrasEnVolgendeStapNaarGetuigen() {
        UUID dossierId = testData.maakCompleetDossier();
        tester.startPage(JullieGegevensPage.class, makeDossierPageParameters(dossierId));

        assertThat(tester.getComponentFromLastRenderedPage(PAGE_BODY + ":volgendeStapContainer")).isNotNull();
        click("volgendeStapContainer:volgendeStap");
        tester.assertRenderedPage(DeGetuigenPage.class);

        click("vorigeStapContainer:vorigeStap");
        tester.assertRenderedPage(JullieGegevensPage.class);

        tester.startPage(ControlerenPage.class, makeDossierPageParameters(dossierId));
        click("vorigeStapContainer:vorigeStap");
        tester.assertRenderedPage(ExtrasPage.class);
    }

    @Test
    @WithMockUser(username = "999990020")
    void sidebarToontAfgerondeOnvoltooideEnActieveStapstatussen() {
        UUID dossierId = testData.maakConceptDossier();
        tester.startPage(DeGetuigenPage.class, makeDossierPageParameters(dossierId));

        assertIconState("algemeenStatusIcon", "rd-status-icon--complete");
        assertIconState("dossierGegevens:datumStatusIcon", "rd-status-icon--upcoming");
        assertIconState("getuigenProgressIcon", "rd-status-icon--current");
        assertIconState("extrasProgressIcon", "rd-status-icon--upcoming");
        assertThat(tester.getComponentFromLastRenderedPage(PAGE_BODY
                + ":keuzesSidebar:getuigenProgressIcon").getDefaultModelObject()).isEqualTo(4);
    }

    private void click(String linkId) {
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.addRequestHeader("sec-fetch-mode", "navigate");
        tester.clickLink(PAGE_BODY + ":" + linkId);
    }

    private void assertIconState(String componentPath, String stateClass) {
        Component component = tester.getComponentFromLastRenderedPage(PAGE_BODY + ":keuzesSidebar:" + componentPath);
        String html = tester.getLastResponseAsString();
        int iconStart = html.indexOf("id=\"" + component.getMarkupId() + "\"");
        assertThat(iconStart).isGreaterThanOrEqualTo(0);
        assertThat(html.substring(iconStart, iconStart + 120)).contains(stateClass);
    }
}
