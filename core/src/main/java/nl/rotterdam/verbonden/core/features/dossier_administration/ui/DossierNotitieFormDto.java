package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import java.io.Serializable;

public class DossierNotitieFormDto implements Serializable {

    private String tekst;

    public String getTekst() {
        return tekst;
    }

    public void setTekst(String tekst) {
        this.tekst = tekst;
    }
}
