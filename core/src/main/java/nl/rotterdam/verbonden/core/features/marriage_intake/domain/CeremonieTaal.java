package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

public enum CeremonieTaal {
    NEDERLANDS("Nederlands"),
    ENGELS("Engels"),
    FRANS("Frans"),
    DUITS("Duits"),
    ANDERS("Anders (tolk nodig)");

    private final String label;

    CeremonieTaal(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
