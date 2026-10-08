package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

/**
 * Hoe een huwelijksdossier tot stand kwam. Online heeft de burger zich met DigiD geïdentificeerd; aan de
 * balie of in een videogesprek controleert een medewerker de identiteit van beide partners.
 */
public enum AanmaakKanaal {

    ONLINE("Online (DigiD)"),
    BALIE("Balie"),
    VIDEO("Videogesprek");

    private final String label;

    AanmaakKanaal(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
