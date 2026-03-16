package nl.rotterdam.huwelijk.features.location_administration.ui;

import nl.rotterdam.huwelijk.features.location_administration.domain.ChangeLocatieDto;

import java.io.Serializable;

public class LocatieFormDto implements Serializable {

    private String naam = "";
    private String fotoUrl = "";

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

    public static LocatieFormDto leeg() {
        return new LocatieFormDto();
    }

    public static LocatieFormDto vanDto(ChangeLocatieDto dto) {
        LocatieFormDto form = new LocatieFormDto();
        form.setNaam(dto.naam());
        form.setFotoUrl(dto.fotoUrl());
        return form;
    }
}
