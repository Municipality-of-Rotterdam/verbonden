package nl.rotterdam.verbonden.core.features.trouwlijst_administration.application;

import nl.rotterdam.verbonden.core.features.dossier_administration.application.DossierAdministrationService;
import nl.rotterdam.verbonden.core.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.ListTrouwlijstDossierDto;
import nl.rotterdam.verbonden.core.features.trouwlijst_administration.domain.TrouwlijstWeekDto;
import nl.rotterdam.verbonden.core.identity.Roles;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.integration_test.InstelbareKlok;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@VerbondenIntegrationTest
@Transactional
@WithMockUser(username = "medewerker", roles = Roles.BEHEER)
class TrouwlijstAdministrationServiceIntegrationTest {

    @Autowired
    private TrouwlijstAdministrationService trouwlijstAdministrationService;

    @Autowired
    private DossierAdministrationService dossierAdministrationService;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private CompleetDossierTestData testData;

    @Autowired
    private InstelbareKlok klok;

    @AfterEach
    void zetKlokTerug() {
        klok.zetTerug();
    }

    /** Alle zeven weekdagen rond een maandovergang, en een jaarovergang. */
    static Stream<LocalDate> testdatums() {
        return Stream.concat(
                Stream.iterate(LocalDate.of(2026, 10, 26), d -> d.plusDays(1)).limit(7),
                Stream.of(LocalDate.of(2026, 12, 31)));
    }

    @ParameterizedTest(name = "vandaag = {0}")
    @MethodSource("testdatums")
    void findWeken_beginntMetVorigeWeekEnTelt53Weken(LocalDate vandaag) {
        klok.zetOp(vandaag);
        LocalDate maandagDezeWeek = vandaag.minusDays(vandaag.getDayOfWeek().getValue() - 1);

        List<TrouwlijstWeekDto> weken = trouwlijstAdministrationService.findWeken();

        assertThat(weken).hasSize(53);
        assertThat(weken.getFirst().van()).isEqualTo(maandagDezeWeek.minusWeeks(1));
        assertThat(weken.getFirst().van().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(weken.getFirst().tot()).isEqualTo(weken.getFirst().van().plusDays(6));
        assertThat(weken.get(1).van()).isEqualTo(maandagDezeWeek);
        assertThat(weken.getLast().van()).isEqualTo(maandagDezeWeek.plusWeeks(51));
    }

    @Test
    void findBevestigdeDossiers_toontAlleenGeaccepteerdeDossiersInDeWeek() {
        UUID dossierId = testData.maakCompleetDossier();
        LocalDate trouwdatum = marriageIntakeService.findByDossierId(dossierId).datumTijdHuwelijk().toLocalDate();
        LocalDate maandag = trouwdatum.minusDays(trouwdatum.getDayOfWeek().getValue() - 1);
        marriageIntakeService.dienIn(dossierId);

        assertThat(trouwlijstAdministrationService.findBevestigdeDossiers(maandag, maandag.plusDays(6))).isEmpty();

        dossierAdministrationService.accepteer(dossierId);

        List<ListTrouwlijstDossierDto> lijst =
                trouwlijstAdministrationService.findBevestigdeDossiers(maandag, maandag.plusDays(6));
        assertThat(lijst).singleElement().satisfies(dto -> {
            assertThat(dto.dossierId()).isEqualTo(dossierId);
            assertThat(dto.datum()).isEqualTo(trouwdatum);
            assertThat(dto.partners()).hasSize(2);
        });
        assertThat(trouwlijstAdministrationService.findBevestigdeDossiers(
                maandag.plusWeeks(1), maandag.plusWeeks(1).plusDays(6))).isEmpty();
    }

    @Test
    void exporteer_maaktExcelMetEenRijPerDossier() throws IOException {
        UUID dossierId = testData.maakCompleetDossier();
        LocalDate trouwdatum = marriageIntakeService.findByDossierId(dossierId).datumTijdHuwelijk().toLocalDate();
        LocalDate maandag = trouwdatum.minusDays(trouwdatum.getDayOfWeek().getValue() - 1);
        marriageIntakeService.dienIn(dossierId);
        dossierAdministrationService.accepteer(dossierId);

        byte[] excel = trouwlijstAdministrationService.exporteer(maandag, maandag.plusDays(6));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            var sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).startsWith("Datum: ");
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).startsWith("Trouwlocatie: ");
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("ABS");
            assertThat(sheet.getRow(3).getCell(3).getStringCellValue()).matches("\\d{2}:\\d{2}");
        }
    }
}
