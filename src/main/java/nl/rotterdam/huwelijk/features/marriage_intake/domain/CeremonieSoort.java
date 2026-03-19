package nl.rotterdam.huwelijk.features.marriage_intake.domain;

public enum CeremonieSoort {

    GRATIS("Gratis", "0,00"),
    EENVOUDIG("Eenvoudig", "267,81"),
    REGULIER("Regulier", "461,48");

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
