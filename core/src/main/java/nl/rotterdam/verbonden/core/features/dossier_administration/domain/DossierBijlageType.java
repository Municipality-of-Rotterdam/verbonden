package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

public enum DossierBijlageType {

    GETUIGENLEGITIMATIE("Getuigenlegitimatie"),
    MUZIEKLIJST("Muzieklijst"),
    TOLKVERKLARING("Tolkverklaring"),
    TROUWLOKATIEVERKLARING("Trouwlokatieverklaring"),
    VERKLARING_GEEN_SCHIJNHUWELIJK("Verklaring geen schijnhuwelijk"),
    WIJZIGING_NAAMGEBRUIK_BUITENLAND("Wijziging naamgebruik (buitenland)"),
    VERKLARING_TROUWAMBTENAAR_1_DAG("Verklaring trouwambtenaar voor 1 dag"),
    CURATORVERKLARING("Curator verklaring"),
    WIJZIGINGSVERZOEK("Wijzigingsverzoek"),
    OVERIGE("Overige");

    private final String label;

    DossierBijlageType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
