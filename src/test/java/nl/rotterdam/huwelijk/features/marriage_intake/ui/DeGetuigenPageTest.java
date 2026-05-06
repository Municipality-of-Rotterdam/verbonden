package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.SaveGetuigenDto;
import nl.rotterdam.huwelijk.integration_test.BaseWicketTest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
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
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, "999990202"));

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
    void renderKleinHuwelijk_toontTweeGetuigenBlokken() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, null, "999990202"));

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
    void slaGetuigenOp_persisteertNamenCorrect() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, "999990202"));

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
    void sidebarToontGedeeltelijkIcoontjeAlsNietAlleGetuigenIngevuld() {
        UUID dossierId = marriageIntakeService.create(
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.GROOT, null, "999990202"));

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
                new CreateDossierDto(RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, null, "999990202"));

        marriageIntakeService.slaGetuigenOp(dossierId, List.of(
                new SaveGetuigenDto(1, "Getuige Een"),
                new SaveGetuigenDto(2, "Getuige Twee")
        ));

        var samenvatting = marriageIntakeService.findByDossierId(dossierId);
        assertThat(samenvatting.getuigenBevestigd()).isTrue();
        assertThat(samenvatting.getuigenGedeeltelijkIngevuld()).isFalse();
    }
}
