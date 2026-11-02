package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.OntbrekendOnderdeel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IntakeStepTest {

    @Test
    void stepsDefineTheirPageAndSequence() {
        assertThat(IntakeStep.values()).containsExactly(
                IntakeStep.MIJN_DAG,
                IntakeStep.DE_DAG,
                IntakeStep.JULLIE_GEGEVENS,
                IntakeStep.DE_GETUIGEN,
                IntakeStep.EXTRAS,
                IntakeStep.CONTROLEREN);
        assertThat(IntakeStep.values()[IntakeStep.EXTRAS.ordinal()].pageClass()).isEqualTo(ExtrasPage.class);
        assertThat(IntakeStep.values()[IntakeStep.CONTROLEREN.ordinal()].ordinal() + 1).isEqualTo(6);
    }

    @Test
    void missingPartsRouteToTheirOwningStep() {
        assertThat(IntakeStep.pageVoor(OntbrekendOnderdeel.DATUM)).isEqualTo(DatumKiezenPage.class);
        assertThat(IntakeStep.pageVoor(OntbrekendOnderdeel.LOCATIE)).isEqualTo(DeDagPage.class);
        assertThat(IntakeStep.pageVoor(OntbrekendOnderdeel.PARTNER_GEKOPPELD)).isEqualTo(JullieGegevensPage.class);
        assertThat(IntakeStep.pageVoor(OntbrekendOnderdeel.ACHTERNAMEN)).isEqualTo(JullieGegevensPage.class);
        assertThat(IntakeStep.pageVoor(OntbrekendOnderdeel.GETUIGEN)).isEqualTo(DeGetuigenPage.class);
    }
}
