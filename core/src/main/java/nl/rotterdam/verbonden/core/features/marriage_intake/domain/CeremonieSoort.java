package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

public enum CeremonieSoort {

    KLEIN("Klein", 2),
    MIDDELGROOT("Middelgroot", 4),
    GROOT("Groot", 4);

    private final String label;
    private final int aantalGetuigen;

    CeremonieSoort(String label, int aantalGetuigen) {
        this.label = label;
        this.aantalGetuigen = aantalGetuigen;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Het aantal getuigen dat bij deze ceremonie hoort.
     */
    public int getAantalGetuigen() {
        return aantalGetuigen;
    }
}
