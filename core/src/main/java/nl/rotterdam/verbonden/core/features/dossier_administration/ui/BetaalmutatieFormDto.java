package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.io.Serializable;

public class BetaalmutatieFormDto implements Serializable {

    private String omschrijving;
    private Bedrag bedrag;

    public String getOmschrijving() {
        return omschrijving;
    }

    public void setOmschrijving(String omschrijving) {
        this.omschrijving = omschrijving;
    }

    public Bedrag getBedrag() {
        return bedrag;
    }

    public void setBedrag(Bedrag bedrag) {
        this.bedrag = bedrag;
    }
}
