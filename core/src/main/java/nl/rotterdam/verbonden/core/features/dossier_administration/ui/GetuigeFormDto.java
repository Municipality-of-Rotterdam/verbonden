package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import java.io.Serializable;

public class GetuigeFormDto implements Serializable {

    private final int volgnummer;
    private String naam;

    public GetuigeFormDto(int volgnummer, String naam) {
        this.volgnummer = volgnummer;
        this.naam = naam;
    }

    public int getVolgnummer() {
        return volgnummer;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }
}
