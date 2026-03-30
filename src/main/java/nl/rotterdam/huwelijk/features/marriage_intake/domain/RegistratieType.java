package nl.rotterdam.huwelijk.features.marriage_intake.domain;

public enum RegistratieType {

    HUWELIJK("Huwelijk"),
    GEREGISTREERD_PARTNERSCHAP("Geregistreerd partnerschap");

    private final String label;

    RegistratieType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
