package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.features.location_administration.application.LocationAdministrationService;
import nl.rotterdam.verbonden.core.features.location_administration.domain.ListBeschikbaarheidDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.IntakeMarriageTypeDto;
import nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData;
import nl.rotterdam.verbonden.core.integration_test.TestOnlineBetaalService;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslag;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagMelding;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagVerwerker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static nl.rotterdam.verbonden.core.integration_test.CompleetDossierTestData.BSN_PARTNER_1;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bewust zonder {@code @Transactional}: een uitslag komt binnen buiten elke transactie (de callback van OBM, de
 * mock-betaalpagina), dus de verwerking moet zelf een transactie openen. Een test die in de transactie van de
 * testklasse draait, ziet het niet als dat misgaat (lazy loading zonder sessie). De testdata wordt daarom
 * opgeruimd in {@link #opruimen()}.
 */
@VerbondenIntegrationTest
class OnlineBetalenZonderTransactieIntegrationTest {

    @Autowired
    private OnlineBetalenService onlineBetalenService;

    @Autowired
    private BetaalUitslagVerwerker verwerker;

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Autowired
    private LocationAdministrationService locationAdministrationService;

    @Autowired
    private CompleetDossierTestData testData;

    @Autowired
    private TestOnlineBetaalService betaalprovider;

    private long locatieId;
    private Set<Long> beschikbaarhedenVooraf;
    private UUID dossierId;

    @BeforeEach
    void onthoudBeschikbaarheden() {
        locatieId = marriageIntakeService.findAllMarriageTypes().stream()
                .filter(type -> type.soort() == CeremonieSoort.KLEIN)
                .map(IntakeMarriageTypeDto::locatieId)
                .findFirst()
                .orElseThrow();
        beschikbaarhedenVooraf = beschikbaarheden();
    }

    @AfterEach
    void opruimen() {
        if (dossierId != null) {
            marriageIntakeService.delete(dossierId);
        }
        beschikbaarheden().stream()
                .filter(id -> !beschikbaarhedenVooraf.contains(id))
                .forEach(locationAdministrationService::deleteBeschikbaarheid);
        betaalprovider.reset();
    }

    @Test
    void uitslagBuitenEenTransactie_dientHetDossierIn() {
        dossierId = testData.maakCompleetBetaaldDossier();
        onlineBetalenService.startBetaling(dossierId, BSN_PARTNER_1, "https://verbonden.test/retour");

        verwerker.verwerkUitslag(new BetaalUitslagMelding(
                betaalprovider.laatsteOpdracht().betalingskenmerk(), null, null, BetaalUitslag.GELUKT));

        assertThat(marriageIntakeService.findStatus(dossierId)).isEqualTo(DossierStatus.INGEDIEND);
    }

    private Set<Long> beschikbaarheden() {
        return locationAdministrationService.findBeschikbaarheden(locatieId).stream()
                .map(ListBeschikbaarheidDto::id)
                .collect(Collectors.toSet());
    }
}
