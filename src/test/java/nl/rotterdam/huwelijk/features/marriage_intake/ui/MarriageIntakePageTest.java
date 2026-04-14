package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.integration_test.BaseWicketTest;
import org.junit.jupiter.api.Test;


class MarriageIntakePageTest extends BaseWicketTest {

    @Test
    void testRender() {
        tester.startPage(MarriageIntakePage.class);
        tester.assertRenderedPage(MarriageIntakePage.class);
    }

}
