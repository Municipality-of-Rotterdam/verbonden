package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.features.dossier_administration.domain.ChangeGetuigeDto;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DetailGetuigeDto;
import nl.rotterdam.verbonden.core.domain.Postcode;

import java.io.Serializable;
import java.time.LocalDate;

public class GetuigeFormDto implements Serializable {

    private final int volgnummer;
    private String naam;
    private String voornamen;
    private String achternaam;
    private LocalDate geboortedatum;
    private Postcode postcode;
    private String woonplaats;
    private String land = ChangeGetuigeDto.NEDERLAND;

    public GetuigeFormDto(int volgnummer) {
        this.volgnummer = volgnummer;
    }

    public static GetuigeFormDto vanDto(DetailGetuigeDto dto) {
        GetuigeFormDto form = new GetuigeFormDto(dto.volgnummer());
        form.setNaam(dto.naam());
        form.setVoornamen(dto.voornamen());
        form.setAchternaam(dto.achternaam());
        form.setGeboortedatum(dto.geboortedatum());
        form.setPostcode(dto.postcode());
        form.setWoonplaats(dto.woonplaats());
        if (dto.land() != null) {
            form.setLand(dto.land());
        }
        return form;
    }

    public ChangeGetuigeDto naarDto() {
        return new ChangeGetuigeDto(volgnummer, naam, voornamen, achternaam, geboortedatum, postcode, woonplaats, land);
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

    public String getVoornamen() {
        return voornamen;
    }

    public void setVoornamen(String voornamen) {
        this.voornamen = voornamen;
    }

    public String getAchternaam() {
        return achternaam;
    }

    public void setAchternaam(String achternaam) {
        this.achternaam = achternaam;
    }

    public LocalDate getGeboortedatum() {
        return geboortedatum;
    }

    public void setGeboortedatum(LocalDate geboortedatum) {
        this.geboortedatum = geboortedatum;
    }

    public Postcode getPostcode() {
        return postcode;
    }

    public void setPostcode(Postcode postcode) {
        this.postcode = postcode;
    }

    public String getWoonplaats() {
        return woonplaats;
    }

    public void setWoonplaats(String woonplaats) {
        this.woonplaats = woonplaats;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }
}
