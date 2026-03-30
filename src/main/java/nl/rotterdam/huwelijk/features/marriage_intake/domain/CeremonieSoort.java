package nl.rotterdam.huwelijk.features.marriage_intake.domain;

public enum CeremonieSoort {

    KLEIN("Klein", "0,00"),
    MIDDELGROOT("Middelgroot", "216,60"),
    GROOT("Groot", "624,40");

    private final String label;
    private final String prijs;

    CeremonieSoort(String label, String prijs) {
        this.label = label;
        this.prijs = prijs;
    }

    public String getLabel() {
        return label;
    }

    public String getPrijs() {
        return prijs;
    }
}
