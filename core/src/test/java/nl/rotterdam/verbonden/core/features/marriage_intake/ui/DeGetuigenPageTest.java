package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveGetuigenDto;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class DeGetuigenPageTest extends BaseWicketTest {

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Test
    @WithMockUser(username = "999990202")
    void renderGrootHuwelijk_toontVierGetuigenBlokken() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990202")));

        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        tester.startPage(DeGetuigenPage.class, params);

        tester.assertRenderedPage(DeGetuigenPage.class);
        tester.assertContains("Getuige 1");
        tester.assertContains("Getuige 2");
        tester.assertContains("Getuige 3");
        tester.assertContains("Getuige 4");
    }

    @Test
    @WithMockUser(username = "999990202")
    void paginaToontGedeeldeStapindeling() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, null, new BurgerServiceNummer("999990202")));
        tester.startPage(DeGetuigenPage.class, new PageParameters().add("dossierId", dossierId.toString()));

        String html = tester.getLastResponseAsString();
        int readAloud = html.indexOf("data-intake-read-aloud");
        int previous = html.indexOf("Vorige stap");
        int stepHeading = html.indexOf("De getuigen", previous);
        int intro = html.indexOf("Geef hieronder de namen", stepHeading);
        int content = html.indexOf("rd-intake-content", intro);
        int next = html.indexOf("Volgende stap", content);

        assertThat(html).contains("Maak uw dag om nooit te vergeten", "rd-intake-step-layout");
        assertThat(readAloud).isGreaterThanOrEqualTo(0);
        assertThat(previous).isGreaterThan(readAloud);
        assertThat(stepHeading).isGreaterThan(previous);
        assertThat(intro).isGreaterThan(stepHeading);
        assertThat(content).isGreaterThan(intro);
        assertThat(next).isGreaterThan(content);
    }

    @Test
    @WithMockUser(username = "999990202")
    void renderKleinHuwelijk_toontTweeGetuigenBlokken() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, null, new BurgerServiceNummer("999990202")));

        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        tester.startPage(DeGetuigenPage.class, params);

        tester.assertRenderedPage(DeGetuigenPage.class);
        tester.assertContains("Getuige 1");
        tester.assertContains("Getuige 2");
        assertThat(tester.getLastResponseAsString()).doesNotContain("Getuige 3");
        assertThat(tester.getLastResponseAsString()).doesNotContain("Getuige 4");
    }

    @Test
    @WithMockUser(username = "999990202")
    void paginaToontBestaandeNaamAlsGetuigeAlIngevuld() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, null, new BurgerServiceNummer("999990202")));
        marriageIntakeService.slaGetuigenOp(dossierId,
                List.of(new SaveGetuigenDto(1, "Kwik van Willegenburgh")));

        PageParameters params = new PageParameters();
        params.add("dossierId", dossierId.toString());
        tester.startPage(DeGetuigenPage.class, params);

        tester.assertRenderedPage(DeGetuigenPage.class);
        assertThat(tester.getLastResponseAsString()).contains("Kwik van Willegenburgh");
    }

    @Test
    @WithMockUser(username = "999990202")
    void slaGetuigenOp_persisteertNamenCorrect() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990202")));

        List<SaveGetuigenDto> getuigen = List.of(
                new SaveGetuigenDto(1, "Kwik van Willegenburgh"),
                new SaveGetuigenDto(2, "Kwek van Willegenburgh"),
                new SaveGetuigenDto(3, "Kwak van Willegenburgh"),
                new SaveGetuigenDto(4, "Dagobert Duck")
        );
        marriageIntakeService.slaGetuigenOp(dossierId, getuigen);

        List<GetuigeDto> opgeslagen = marriageIntakeService.findGetuigen(dossierId);
        assertThat(opgeslagen).hasSize(4);
        assertThat(opgeslagen.get(0).naam()).isEqualTo("Kwik van Willegenburgh");
        assertThat(opgeslagen.get(3).naam()).isEqualTo("Dagobert Duck");
    }

    @Test
    @WithMockUser(username = "999990202")
    void slaGetuigeOp_persisteertEnkeleGetuige() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990202")));

        marriageIntakeService.slaGetuigeOp(dossierId, new SaveGetuigenDto(2, "Anna van Bergen"));

        List<GetuigeDto> opgeslagen = marriageIntakeService.findGetuigen(dossierId);
        assertThat(opgeslagen).hasSize(1);
        assertThat(opgeslagen.getFirst().volgnummer()).isEqualTo(2);
        assertThat(opgeslagen.getFirst().naam()).isEqualTo("Anna van Bergen");
    }

    @Test
    @WithMockUser(username = "999990202")
    void slaGetuigeOp_updatesBestaandeGetuigeZonderDuplicaat() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990202")));
        marriageIntakeService.slaGetuigeOp(dossierId, new SaveGetuigenDto(1, "Kwik van Willegenburgh"));

        marriageIntakeService.slaGetuigeOp(dossierId, new SaveGetuigenDto(1, "Kwek van Willegenburgh"));

        List<GetuigeDto> opgeslagen = marriageIntakeService.findGetuigen(dossierId);
        assertThat(opgeslagen).hasSize(1);
        assertThat(opgeslagen.getFirst().naam()).isEqualTo("Kwek van Willegenburgh");
    }

    @Test
    @WithMockUser(username = "999990202")
    void slaGetuigenOp_negerteBlanckeNamen() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990202")));

        marriageIntakeService.slaGetuigenOp(dossierId, List.of(
                new SaveGetuigenDto(1, "Anna van Bergen"),
                new SaveGetuigenDto(2, ""),
                new SaveGetuigenDto(3, "  ")
        ));

        List<GetuigeDto> opgeslagen = marriageIntakeService.findGetuigen(dossierId);
        assertThat(opgeslagen).hasSize(1);
        assertThat(opgeslagen.getFirst().naam()).isEqualTo("Anna van Bergen");
    }

    @Test
    @WithMockUser(username = "999990202")
    void findGetuigen_retourneertGeorderdOpVolgnummer() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990202")));
        marriageIntakeService.slaGetuigenOp(dossierId, List.of(
                new SaveGetuigenDto(3, "Derde Getuige"),
                new SaveGetuigenDto(1, "Eerste Getuige"),
                new SaveGetuigenDto(2, "Tweede Getuige")
        ));

        List<GetuigeDto> opgeslagen = marriageIntakeService.findGetuigen(dossierId);

        assertThat(opgeslagen).extracting(GetuigeDto::volgnummer).containsExactly(1, 2, 3);
    }

    @Test
    @WithMockUser(username = "999990202")
    void sidebarToontGedeeltelijkIcoontjeAlsNietAlleGetuigenIngevuld() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990202")));

        marriageIntakeService.slaGetuigenOp(dossierId,
                List.of(new SaveGetuigenDto(1, "Eerste Getuige")));

        var samenvatting = marriageIntakeService.findByDossierId(dossierId);
        assertThat(samenvatting.getuigenBevestigd()).isFalse();
        assertThat(samenvatting.getuigenGedeeltelijkIngevuld()).isTrue();
    }

    @Test
    @WithMockUser(username = "999990202")
    void sidebarToontGroenVinkjeAlsAlleGetuigenIngevuld() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, null, new BurgerServiceNummer("999990202")));

        marriageIntakeService.slaGetuigenOp(dossierId, List.of(
                new SaveGetuigenDto(1, "Getuige Een"),
                new SaveGetuigenDto(2, "Getuige Twee")
        ));

        var samenvatting = marriageIntakeService.findByDossierId(dossierId);
        assertThat(samenvatting.getuigenBevestigd()).isTrue();
        assertThat(samenvatting.getuigenGedeeltelijkIngevuld()).isFalse();
    }

    @Test
    @WithMockUser(username = "999990202")
    void sidebarToontGeenBevestigingAlsGeenGetuigenIngevuld() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, new BurgerServiceNummer("999990202")));

        var samenvatting = marriageIntakeService.findByDossierId(dossierId);
        assertThat(samenvatting.getuigenBevestigd()).isFalse();
        assertThat(samenvatting.getuigenGedeeltelijkIngevuld()).isFalse();
    }

    @Test
    @WithMockUser(username = "999990202")
    void getuigeOpslaanViaAjax_herlaadtDePaginaNiet() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, null, new BurgerServiceNummer("999990202")));
        tester.startPage(DeGetuigenPage.class, new PageParameters().add("dossierId", dossierId.toString()));

        FormComponent<?> naam = naamveldVanGetuige(1);
        tester.getRequest().getPostParameters().setParameterValue(naam.getInputName(), "Jan Jansen");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        tester.executeBehavior(naam.getBehaviors(AjaxFormComponentUpdatingBehavior.class).getFirst());

        // Geen nieuwe page-request (die springt naar boven): alleen het veld en de sidebar worden ververst
        tester.assertRenderedPage(DeGetuigenPage.class);
        tester.assertComponentOnAjaxResponse("pageLayout:pageLayout_body:pageBody:pageBody_body:keuzesSidebar");
        assertThat(ajaxComponent("pageLayout:pageLayout_body:pageBody:pageBody_body:keuzesSidebar")).contains("Jan Jansen");
        assertThat(marriageIntakeService.findGetuigen(dossierId))
                .extracting(GetuigeDto::naam)
                .containsExactly("Jan Jansen");
    }

    /**
     * De markup van een component in het laatste Ajax-antwoord.
     */
    private String ajaxComponent(String pad) {
        String markupId = tester.getComponentFromLastRenderedPage(pad).getMarkupId();
        String response = tester.getLastResponseAsString();
        int start = response.indexOf("<component id=\"" + markupId + "\"");
        assertThat(start).as("component %s in Ajax-antwoord", markupId).isGreaterThanOrEqualTo(0);
        return response.substring(start, response.indexOf("</component>", start));
    }

    private FormComponent<?> naamveldVanGetuige(int volgnummer) {
        String pad = "getuigen:" + (volgnummer - 1) + ":getuigeForm";
        return tester.getLastRenderedPage().visitChildren(FormComponent.class, (FormComponent<?> component, IVisit<FormComponent<?>> visit) -> {
            if (component.getPageRelativePath().contains(pad)) {
                visit.stop(component);
            }
        });
    }
}
