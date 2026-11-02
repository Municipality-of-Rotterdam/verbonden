package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import org.apache.wicket.util.tester.TagTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Zoekt de statusiconen van de intake-sidebar in de gerenderde HTML. Ze staan in dezelfde volgorde als de stappen in
 * {@link IntakeStep}.
 */
final class SidebarStatusIconen {

    private SidebarStatusIconen() {
    }

    static TagTester statusIcoon(String html, IntakeStep step) {
        List<TagTester> iconen = TagTester.createTags(html, tag -> {
            String cssClass = tag.getAttributes().getString("class");
            return cssClass != null && cssClass.startsWith("rd-status-icon ");
        }, false);
        assertThat(iconen).hasSize(IntakeStep.values().length);
        return iconen.get(step.ordinal());
    }
}
