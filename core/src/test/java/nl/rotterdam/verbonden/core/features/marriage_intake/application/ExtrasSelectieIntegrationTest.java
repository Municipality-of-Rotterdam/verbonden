package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveExtrasDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SidebarExtraItemDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.TrouwboekjeKeuzeDto;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.application.TrouwboekjeAdministrationService;
import nl.rotterdam.verbonden.core.features.trouwboekje_administration.domain.CreateTrouwboekjeDto;
import nl.rotterdam.verbonden.core.integration_test.InstelbareKlok;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@VerbondenIntegrationTest
@Transactional
class ExtrasSelectieIntegrationTest {

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private TrouwboekjeAdministrationService trouwboekjeAdministrationService;

    @Autowired
    private InstelbareKlok klok;

    @AfterEach
    void zetKlokTerug() {
        klok.zetTerug();
    }

    /** Het tarief gaat op 1 januari 2027 omhoog; een nog niet ingediend dossier toont het tarief van vandaag. */
    @ParameterizedTest(name = "vandaag = {0}")
    @CsvSource({
            "2026-10-26, 17.80",
            "2026-12-31, 17.80",
            "2027-01-01, 18.30",
            "2027-06-15, 18.30"})
    void huwelijk_slaatInternationaleAkteOp(LocalDate vandaag, String tariefVandaag) {
        Bedrag verwachtTarief = Bedrag.of(tariefVandaag);
        klok.zetOp(vandaag);
        UUID dossierId = maakDossier(RegistratieType.HUWELIJK, "999990007");

        marriageIntakeService.slaExtrasOp(dossierId, new SaveExtrasDto(false, false, null, true));

        assertThat(marriageIntakeService.findExtrasSelecties(dossierId).internationaleAkte()).isTrue();
        assertThat(marriageIntakeService.findByDossierId(dossierId).extras())
                .extracting(SidebarExtraItemDto::naam, SidebarExtraItemDto::prijs)
                .containsExactly(tuple("Internationale huwelijksakte", verwachtTarief));
        assertThat(marriageIntakeService.findInternationaleAktePrijs(dossierId)).isEqualTo(verwachtTarief);
    }

    @Test
    void geregistreerdPartnerschap_negeertInternationaleAkte() {
        UUID dossierId = maakDossier(RegistratieType.GEREGISTREERD_PARTNERSCHAP, "999990202");

        marriageIntakeService.slaExtrasOp(dossierId, new SaveExtrasDto(false, false, null, true));

        assertThat(marriageIntakeService.findExtrasSelecties(dossierId).internationaleAkte()).isFalse();
    }

    @Test
    void gekozenTrouwboekje_wordtOpgeslagenEnMetPrijsInSidebarGetoond() {
        long trouwboekjeId = trouwboekjeAdministrationService.create(new CreateTrouwboekjeDto(
                "Linnen trouwboekje", null, "/trouwboekjes/linnen.webp", Bedrag.of("42.50"), null, null));
        UUID dossierId = maakDossier(RegistratieType.HUWELIJK, "999990019");

        assertThat(marriageIntakeService.findActieveTrouwboekjes())
                .extracting(TrouwboekjeKeuzeDto::id, TrouwboekjeKeuzeDto::afbeelding)
                .contains(tuple(trouwboekjeId, "/trouwboekjes/linnen.webp"));

        marriageIntakeService.slaExtrasOp(dossierId, new SaveExtrasDto(false, false, trouwboekjeId, false));

        assertThat(marriageIntakeService.findExtrasSelecties(dossierId).trouwboekjeId()).isEqualTo(trouwboekjeId);
        assertThat(marriageIntakeService.findByDossierId(dossierId).extras())
                .containsExactly(new SidebarExtraItemDto("Linnen trouwboekje", Bedrag.of("42.50")));
    }

    /** Een trouwboekje is te kiezen vanaf de startdatum, tot (niet tot en met) de einddatum. */
    @ParameterizedTest(name = "vandaag = {0}: kiesbaar = {1}")
    @CsvSource({
            "2026-10-31, false",
            "2026-11-01, true",
            "2026-11-30, true",
            "2026-12-01, false"})
    void trouwboekje_isAlleenTussenStartEnEinddatumTeKiezen(LocalDate vandaag, boolean kiesbaar) {
        klok.zetOp(vandaag);
        long trouwboekjeId = trouwboekjeAdministrationService.create(new CreateTrouwboekjeDto(
                "Seizoensboekje", null, "/trouwboekjes/seizoen.webp", Bedrag.of("30.00"),
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 12, 1)));

        assertThat(marriageIntakeService.findActieveTrouwboekjes())
                .extracting(TrouwboekjeKeuzeDto::id)
                .satisfies(ids -> assertThat(ids.contains(trouwboekjeId)).isEqualTo(kiesbaar));
    }

    private UUID maakDossier(RegistratieType registratieType, String bsn) {
        return marriageIntakeService.create(
                new CreateDossierDto(registratieType, CeremonieSoort.KLEIN, null, new BurgerServiceNummer(bsn)));
    }
}
