package nl.rotterdam.verbonden.core.features.location_administration.ui;

import nl.rotterdam.verbonden.core.features.location_administration.application.LocationAdministrationService;
import nl.rotterdam.verbonden.core.features.location_administration.domain.CreateBeschikbaarheidDto;
import nl.rotterdam.verbonden.core.features.location_administration.domain.CreateLocatieDto;
import nl.rotterdam.verbonden.core.features.location_administration.domain.HuwelijksType;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.BaseWicketTest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@WithMockUser(username = "medewerker", roles = Roles.BEHEER)
class LocationBeschikbaarheidPageTest extends BaseWicketTest {

    @Autowired
    private LocationAdministrationService locationAdministrationService;

    @Test
    void aanmaakPagina_slaatNumeriekeEnDatumveldenOp() {
        long locatieId = maakLocatie();
        tester.startPage(BeschikbaarheidCreatePage.class, new PageParameters().add("locatieId", locatieId));

        FormTester form = formTester("beschikbaarheidForm");
        form.setValue(veld("beschikbaarheidForm", "huwelijkstype"), HuwelijksType.REGULIER.name());
        form.setValue(veld("beschikbaarheidForm", "dagVanDeWeek"), DayOfWeek.WEDNESDAY.name());
        form.setValue(veld("beschikbaarheidForm", "startTijd"), "09:15");
        form.setValue(veld("beschikbaarheidForm", "eindTijd"), "10:00");
        form.setValue(veld("beschikbaarheidForm", "duurInMinuten"), "45");
        form.setValue(veld("beschikbaarheidForm", "prijs"), "1234.56");
        form.setValue(veld("beschikbaarheidForm", "ingangsdatum"), "2026-03-01");
        form.setValue(veld("beschikbaarheidForm", "einddatum"), "2026-12-31");
        tester.addRequestHeader("sec-fetch-site", "same-origin");
        form.submit(veld("beschikbaarheidForm", "opslaan"));

        tester.assertNoErrorMessage();
        tester.assertRenderedPage(LocationUpdatePage.class);
        var beschikbaarheid = locationAdministrationService.findBeschikbaarheden(locatieId).getFirst();
        assertThat(beschikbaarheid.duurInMinuten()).isEqualTo(45);
        assertThat(beschikbaarheid.prijs()).isEqualByComparingTo("1234.56");
        assertThat(beschikbaarheid.ingangsdatum()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(beschikbaarheid.einddatum()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    void detailPagina_toontPrijsInNederlandseValutanotatie() {
        long locatieId = maakLocatie();
        locationAdministrationService.createBeschikbaarheid(new CreateBeschikbaarheidDto(
                locatieId,
                HuwelijksType.REGULIER,
                DayOfWeek.WEDNESDAY,
                LocalTime.of(9, 15),
                LocalTime.of(10, 0),
                45,
                new BigDecimal("1234.56"),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        ));

        tester.startPage(LocationUpdatePage.class, new PageParameters().add("id", locatieId));

        assertThat(tester.getLastResponseAsString())
                .contains("€")
                .contains("1.234,56");
    }

    private long maakLocatie() {
        return locationAdministrationService.create(new CreateLocatieDto("Testlocatie", null, null, null));
    }
}
