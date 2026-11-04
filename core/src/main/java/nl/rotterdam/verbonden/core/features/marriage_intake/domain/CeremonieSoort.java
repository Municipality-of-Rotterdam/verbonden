package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

public enum CeremonieSoort {

    KLEIN("Klein", 2, 4),
    MIDDELGROOT("Middelgroot", 4, 16),
    GROOT("Groot", 4, 100);

    private final String label;
    private final int aantalGetuigen;
    private final int maxAantalBezoekers;

    CeremonieSoort(String label, int aantalGetuigen, int maxAantalBezoekers) {
        this.label = label;
        this.aantalGetuigen = aantalGetuigen;
        this.maxAantalBezoekers = maxAantalBezoekers;
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

    /**
     * Het maximale aantal bezoekers bij deze ceremonie;
     */
    public int getMaxAantalBezoekers() {
        return maxAantalBezoekers;
    }
}
