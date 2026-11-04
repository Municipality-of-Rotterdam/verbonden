package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieTaal;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.ChangeIntakeDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CreateDossierDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.OntbrekendOnderdeel;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;
import nl.rotterdam.verbonden.core.integration_test.VerbondenIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@VerbondenIntegrationTest
@Transactional
class TaalEnAantalBezoekersIntegrationTest {

    @Autowired
    private MarriageIntakeService marriageIntakeService;

    @Test
    void nieuwDossier_mistTaalEnAantalBezoekers() {
        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        assertThat(marriageIntakeService.findByDossierId(dossierId).ontbrekendeOnderdelen())
                .contains(OntbrekendOnderdeel.TAAL, OntbrekendOnderdeel.AANTAL_BEZOEKERS);
    }

    @Test
    void taalEnAantalBezoekers_wordenBijHetDossierOpgeslagen() {
        UUID dossierId = maakDossier(CeremonieSoort.KLEIN);

        marriageIntakeService.slaTaalOp(dossierId, CeremonieTaal.FRANS);
        marriageIntakeService.slaAantalBezoekersOp(dossierId, 3);

        var dossier = marriageIntakeService.findByDossierId(dossierId);
        assertThat(dossier.taal()).isEqualTo(CeremonieTaal.FRANS);
        assertThat(dossier.aantalBezoekers()).isEqualTo(3);
        assertThat(dossier.ontbrekendeOnderdelen())
                .doesNotContain(OntbrekendOnderdeel.TAAL, OntbrekendOnderdeel.AANTAL_BEZOEKERS);
    }

    @ParameterizedTest(name = "{0}: {1} bezoekers toegestaan = {2}")
    @CsvSource({
            "KLEIN, 4, true",
            "KLEIN, 5, false",
            "MIDDELGROOT, 16, true",
            "MIDDELGROOT, 17, false",
            "GROOT, 100, true",
            "GROOT, 101, false",
            "GROOT, -1, false"})
    void aantalBezoekers_isBeperktDoorHetMaximumVanDeCeremonie(CeremonieSoort soort, int aantal, boolean toegestaan) {
        UUID dossierId = maakDossier(soort);

        if (toegestaan) {
            marriageIntakeService.slaAantalBezoekersOp(dossierId, aantal);
            assertThat(marriageIntakeService.findByDossierId(dossierId).aantalBezoekers()).isEqualTo(aantal);
        } else {
            assertThatThrownBy(() -> marriageIntakeService.slaAantalBezoekersOp(dossierId, aantal))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void kleinereCeremonie_latHetTeGroteAantalBezoekersVervallen() {
        UUID dossierId = maakDossier(CeremonieSoort.GROOT);
        marriageIntakeService.slaAantalBezoekersOp(dossierId, 40);

        marriageIntakeService.updateIntake(dossierId,
                new ChangeIntakeDto(RegistratieType.HUWELIJK, CeremonieSoort.KLEIN, null));

        assertThat(marriageIntakeService.findByDossierId(dossierId).aantalBezoekers()).isNull();
    }

    private UUID maakDossier(CeremonieSoort soort) {
        return marriageIntakeService.create(new CreateDossierDto(
                RegistratieType.HUWELIJK, soort, null, new BurgerServiceNummer("999990007")));
    }
}
