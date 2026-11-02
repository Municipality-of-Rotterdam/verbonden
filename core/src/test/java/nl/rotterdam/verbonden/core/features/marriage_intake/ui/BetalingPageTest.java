package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

@Transactional
class BetalingPageTest extends BaseWicketTest {

    private static final String PAYMENT_BUTTON =
            "pageLayout:pageLayout_body:pageBody:pageBody_body:betalingForm:betalingButton";

    @Autowired
    private CompleetDossierTestData testData;

    @Test
    @WithMockUser(username = "999990020")
    void betalenBlijftActiefVoorEenCompleetDossier() {
        tester.startPage(BetalingPage.class, makeDossierPageParameters(testData.maakCompleetDossier()));

        tester.assertEnabled(PAYMENT_BUTTON);
    }

    @Test
    @WithMockUser(username = "999990020")
    void betalenBlijftActiefVoorEenOnvolledigDossier() {
        tester.startPage(BetalingPage.class, makeDossierPageParameters(testData.maakConceptDossier()));

        tester.assertEnabled(PAYMENT_BUTTON);
    }
}
