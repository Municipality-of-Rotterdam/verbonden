package nl.rotterdam.verbonden.core.features.babs_administration.ui;

import nl.rotterdam.verbonden.core.domain.PersonFullName;
import nl.rotterdam.verbonden.core.features.babs_administration.application.BabsAdministrationService;
import nl.rotterdam.verbonden.core.features.babs_administration.domain.ChangeBabsDto;
import nl.rotterdam.verbonden.core.features.babs_administration.domain.CreateBabsDto;
import nl.rotterdam.verbonden.core.features.babs_administration.domain.ListBabsDto;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@WithMockUser(username = "medewerker", roles = Roles.BEHEER)
class BabsBeheerPaginaTest extends BaseWicketTest {

    private static final PersonFullName NAAM = new PersonFullName("Jan de Testambtenaar");

    @Autowired
    private BabsAdministrationService babsAdministrationService;

    @Test
    void aanmaakPagina_maaktBabsAan() {
        tester.startPage(BabsCreatePage.class);

        FormTester form = formTester("babsForm");
        form.setValue(veld("babsForm", "naam"), NAAM.getValue());
        form.setValue(veld("babsForm", "actief"), true);
        form.setValue(veld("babsForm", "actiefVanaf"), "2026-01-01");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("babsForm", "opslaan"));

        tester.assertNoErrorMessage();
        tester.assertRenderedPage(BabsAdministrationPage.class);
        ListBabsDto babs = zoekOpNaam(NAAM).orElseThrow();
        assertThat(babs.actief()).isTrue();
        assertThat(babs.actiefVanaf()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void overzichtToontIconActiesMetTooltips() {
        babsAdministrationService.create(new CreateBabsDto(
                NAAM, null, null, true, null, null, List.of(DayOfWeek.MONDAY)));
        babsAdministrationService.create(new CreateBabsDto(
                new PersonFullName("Piet de Testambtenaar"), null, null, false, null, null,
                List.of(DayOfWeek.MONDAY)));

        tester.startPage(BabsAdministrationPage.class);

        assertThat(tester.getLastResponseAsString())
                .contains("title=\"Bewerken\"")
                .contains("title=\"Inactief maken\"")
                .contains("title=\"Actief maken\"")
                .contains("rods-icon-edit")
                .contains("rods-icon-badge-alert")
                .contains("rods-icon-badge-check")
                .doesNotContain(">Bewerken</a>", ">Actief maken</a>", ">Inactief maken</a>");
    }

    @Test
    void aanmaakPagina_teKorteNaam_toontMeldingEnMaaktGeenBabsAan() {
        long aantalVoor = babsAdministrationService.count();
        tester.startPage(BabsCreatePage.class);

        FormTester form = formTester("babsForm");
        form.setValue(veld("babsForm", "naam"), "Jan");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("babsForm", "opslaan"));

        tester.assertRenderedPage(BabsCreatePage.class);
        assertThat(tester.getLastResponseAsString()).contains("Naam is korter dan 5 tekens");
        assertThat(babsAdministrationService.count()).isEqualTo(aantalVoor);
    }

    @Test
    void aanmaakPagina_actiefTotEnMetVoorActiefVanaf_toontMeldingEnMaaktGeenBabsAan() {
        tester.startPage(BabsCreatePage.class);

        FormTester form = formTester("babsForm");
        form.setValue(veld("babsForm", "naam"), NAAM.getValue());
        form.setValue(veld("babsForm", "actiefVanaf"), "2026-06-01");
        form.setValue(veld("babsForm", "actiefTotEnMet"), "2026-05-31");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("babsForm", "opslaan"));

        tester.assertRenderedPage(BabsCreatePage.class);
        assertThat(tester.getLastResponseAsString()).contains("Actief tot en met mag niet vóór actief vanaf liggen");
        assertThat(zoekOpNaam(NAAM)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "2026-06-01, 2026-06-01",
            "2026-06-01, 2026-06-02",
            "2026-06-01, ",
            ", 2026-06-01",
            ", "
    })
    void aanmaakPagina_geldigeActiefPeriode_maaktBabsAan(String actiefVanaf, String actiefTotEnMet) {
        tester.startPage(BabsCreatePage.class);

        FormTester form = formTester("babsForm");
        form.setValue(veld("babsForm", "naam"), NAAM.getValue());
        form.setValue(veld("babsForm", "actiefVanaf"), actiefVanaf == null ? "" : actiefVanaf);
        form.setValue(veld("babsForm", "actiefTotEnMet"), actiefTotEnMet == null ? "" : actiefTotEnMet);
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("babsForm", "opslaan"));

        tester.assertNoErrorMessage();
        tester.assertRenderedPage(BabsAdministrationPage.class);
        assertThat(zoekOpNaam(NAAM)).isPresent();
    }

    @Test
    void wijzigPagina_actiefTotEnMetVoorActiefVanaf_toontMeldingEnWijzigtNiets() {
        long id = babsAdministrationService.create(new CreateBabsDto(NAAM, null, null, true,
                LocalDate.of(2026, 1, 1), null, List.of(DayOfWeek.MONDAY)));

        tester.startPage(BabsUpdatePage.class, new PageParameters().add("id", id));
        FormTester form = formTester("babsForm");
        form.setValue(veld("babsForm", "actiefTotEnMet"), "2025-12-31");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("babsForm", "opslaan"));

        tester.assertRenderedPage(BabsUpdatePage.class);
        assertThat(tester.getLastResponseAsString()).contains("Actief tot en met mag niet vóór actief vanaf liggen");
        assertThat(babsAdministrationService.findById(id))
                .get()
                .extracting(ChangeBabsDto::actiefTotEnMet)
                .isNull();
    }

    @Test
    void wijzigPagina_wijzigtNaam() {
        long id = babsAdministrationService.create(new CreateBabsDto(NAAM, null, null, true,
                LocalDate.of(2026, 1, 1), null, List.of(DayOfWeek.MONDAY)));
        PersonFullName nieuweNaam = new PersonFullName("Jan de Gewijzigde");

        tester.startPage(BabsUpdatePage.class, new PageParameters().add("id", id));
        FormTester form = formTester("babsForm");
        form.setValue(veld("babsForm", "naam"), nieuweNaam.getValue());
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("babsForm", "opslaan"));

        tester.assertNoErrorMessage();
        tester.assertRenderedPage(BabsAdministrationPage.class);
        assertThat(babsAdministrationService.findById(id))
                .get()
                .extracting(ChangeBabsDto::naam)
                .isEqualTo(nieuweNaam);
    }

    private Optional<ListBabsDto> zoekOpNaam(PersonFullName naam) {
        return babsAdministrationService.findAll(Pageable.unpaged()).stream()
                .filter(b -> b.naam().equals(naam))
                .findFirst();
    }
}
