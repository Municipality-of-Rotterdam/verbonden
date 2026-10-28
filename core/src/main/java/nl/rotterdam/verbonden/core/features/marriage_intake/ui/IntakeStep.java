package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.OntbrekendOnderdeel;

public enum IntakeStep {
    MIJN_DAG(MarriageIntakePage.class),
    DE_DAG(DeDagPage.class),
    JULLIE_GEGEVENS(JullieGegevensPage.class),
    DE_GETUIGEN(DeGetuigenPage.class),
    EXTRAS(ExtrasPage.class),
    CONTROLEREN(ControlerenPage.class);

    private final Class<? extends IntakeBasePage> pageClass;

    IntakeStep(Class<? extends IntakeBasePage> pageClass) {
        this.pageClass = pageClass;
    }

    public Class<? extends IntakeBasePage> pageClass() {
        return pageClass;
    }

    public static Class<? extends IntakeBasePage> pageVoor(OntbrekendOnderdeel onderdeel) {
        return switch (onderdeel) {
            case DATUM -> DatumKiezenPage.class;
            case LOCATIE -> DE_DAG.pageClass;
            case PARTNER_GEKOPPELD, ACHTERNAMEN -> JULLIE_GEGEVENS.pageClass;
            case GETUIGEN -> DE_GETUIGEN.pageClass;
        };
    }
}
