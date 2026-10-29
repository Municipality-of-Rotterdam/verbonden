package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.OntbrekendOnderdeel;
import org.apache.wicket.Page;

public enum IntakeStep {
    MIJN_DAG(MarriageIntakePage.class),
    DE_DAG(DeDagPage.class),
    JULLIE_GEGEVENS(JullieGegevensPage.class),
    DE_GETUIGEN(DeGetuigenPage.class),
    EXTRAS(ExtrasPage.class),
    CONTROLEREN(ControlerenPage.class);

    private final Class<? extends Page> pageClass;

    IntakeStep(Class<? extends Page> pageClass) {
        this.pageClass = pageClass;
    }

    public Class<? extends Page> pageClass() {
        return pageClass;
    }

    public static Class<? extends Page> pageVoor(OntbrekendOnderdeel onderdeel) {
        return switch (onderdeel) {
            case DATUM -> DatumKiezenPage.class;
            case LOCATIE -> DE_DAG.pageClass;
            case PARTNER_GEKOPPELD, ACHTERNAMEN -> JULLIE_GEGEVENS.pageClass;
            case GETUIGEN -> DE_GETUIGEN.pageClass;
        };
    }
}
