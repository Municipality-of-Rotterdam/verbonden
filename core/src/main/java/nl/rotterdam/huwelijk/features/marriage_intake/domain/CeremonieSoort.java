package nl.rotterdam.huwelijk.features.marriage_intake.domain;

public enum CeremonieSoort {

    KLEIN("Klein"),
    MIDDELGROOT("Middelgroot"),
    GROOT("Groot");

    private final String label;

    CeremonieSoort(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
