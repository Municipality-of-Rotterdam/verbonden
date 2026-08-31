package nl.rotterdam.verbonden.core.features.location_administration.ui;

import nl.rotterdam.verbonden.core.features.location_administration.domain.ChangeLocatieDto;

import java.io.Serializable;

public class LocatieFormDto implements Serializable {

    private String naam = "";
    private String fotoUrl = "";
    private String omschrijving = "";
    private String detailUrl = "";

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getOmschrijving() {
        return omschrijving;
    }

    public void setOmschrijving(String omschrijving) {
        this.omschrijving = omschrijving;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public static LocatieFormDto leeg() {
        return new LocatieFormDto();
    }

    public static LocatieFormDto vanDto(ChangeLocatieDto dto) {
        LocatieFormDto form = new LocatieFormDto();
        form.setNaam(dto.naam());
        form.setFotoUrl(dto.fotoUrl());
        form.setOmschrijving(dto.omschrijving());
        form.setDetailUrl(dto.detailUrl());
        return form;
    }
}
