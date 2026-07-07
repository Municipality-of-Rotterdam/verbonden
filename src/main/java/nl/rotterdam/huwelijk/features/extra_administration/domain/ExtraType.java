package nl.rotterdam.huwelijk.features.extra_administration.domain;

public enum ExtraType {
    TROUWBOEKJE("Trouwboekje"),
    INTERNATIONALE_AKTE("Internationale akte");

    private final String label;

    ExtraType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
