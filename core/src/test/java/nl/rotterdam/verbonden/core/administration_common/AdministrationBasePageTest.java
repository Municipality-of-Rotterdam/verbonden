package nl.rotterdam.verbonden.core.administration_common;

import nl.rotterdam.verbonden.core.features.babs_administration.ui.BabsAdministrationPage;
import nl.rotterdam.verbonden.core.features.location_administration.ui.LocationAdministrationPage;
import nl.rotterdam.verbonden.core.features.location_administration.ui.LocationCreatePage;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.util.tester.TagTester;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AdministrationBasePageTest extends BaseWicketTest {

    @Test
    @WithMockUser(roles = Roles.BEHEER)
    void toontSidebarMetBeheermenuEnZonderBovenOfOnderbalk() {
        tester.startPage(BabsAdministrationPage.class);

        String response = tester.getLastResponseAsString();
        assertThat(response)
                .contains("administration-sidebar-toggle")
                .contains("BABS")
                .contains("Trouwlocaties")
                .contains("Huwelijkstypen")
                .contains("Dossiers")
                .contains("Trouwboekjes")
                .contains("Uitloggen")
                .contains("Beheerder")
                .contains("href=\"/logout\"")
                .doesNotContain("rd-top-bar")
                .doesNotContain("rd-footer-links")
                .doesNotContain("pageHeader")
                .doesNotContain("pageFooter");
    }

    @Test
    @WithMockUser(roles = Roles.BEHEER)
    void toontHetVerbondenLogo() {
        tester.startPage(BabsAdministrationPage.class);

        assertThat(tester.getLastResponseAsString()).contains("src=\"/images/verbonden-logo.svg\"");
        assertThat(getClass().getResource("/static/images/verbonden-logo.svg")).isNotNull();
    }

    @Test
    @WithMockUser(roles = Roles.BEHEER)
    void markeertHetMenuItemVanDeHuidigePagina() {
        tester.startPage(LocationAdministrationPage.class);

        assertThat(gemarkeerdeMenuItems())
                .singleElement()
                .satisfies(link -> {
                    assertThat(link.getAttribute("aria-current")).isEqualTo("page");
                    assertThat(link.getAttribute("title")).isEqualTo("Trouwlocaties");
                });
    }

    @Test
    @WithMockUser(roles = Roles.BEHEER)
    void markeertOpEenVervolgpaginaHetMenuItemVanHetOverzicht() {
        tester.startPage(LocationCreatePage.class);

        assertThat(gemarkeerdeMenuItems())
                .singleElement()
                .satisfies(link -> {
                    assertThat(link.getAttribute("aria-current")).isEqualTo("true");
                    assertThat(link.getAttribute("title")).isEqualTo("Trouwlocaties");
                });
    }

    @Test
    @WithMockUser(roles = Roles.BEHEER)
    void zijbalkIsStandaardIngeklapt() {
        tester.startPage(BabsAdministrationPage.class);

        assertThat(sidebarToggle().getAttribute("checked")).isNull();
    }

    @Test
    @WithMockUser(roles = Roles.BEHEER)
    void onthoudtDatDeZijbalkIsUitgeklapt() {
        tester.startPage(BabsAdministrationPage.class);
        CheckBox toggle = (CheckBox) tester.getComponentFromLastRenderedPage("sidebarToggle");
        tester.getRequest().getPostParameters().setParameterValue(toggle.getInputName(), "true");
        tester.executeAjaxEvent(toggle, "change");

        tester.startPage(LocationAdministrationPage.class);

        assertThat(sidebarToggle().getAttribute("checked")).isNotNull();
    }

    private List<TagTester> gemarkeerdeMenuItems() {
        String response = tester.getLastResponseAsString();
        return Stream.of("page", "true")
                .flatMap(waarde -> TagTester.createTagsByAttribute(response, "aria-current", waarde, false).stream())
                .toList();
    }

    private TagTester sidebarToggle() {
        // De <label for> in de markup verwijst naar dit id
        return TagTester.createTagByAttribute(tester.getLastResponseAsString(), "id", "administration-sidebar-toggle");
    }
}
