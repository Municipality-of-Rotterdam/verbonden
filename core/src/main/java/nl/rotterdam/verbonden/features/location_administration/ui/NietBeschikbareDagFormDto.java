package nl.rotterdam.verbonden.features.location_administration.ui;

import nl.rotterdam.verbonden.features.location_administration.domain.ChangeNietBeschikbareDagDto;

import java.io.Serializable;
import java.time.LocalDate;

public class NietBeschikbareDagFormDto implements Serializable {

    private LocalDate datum = null;
    private String reden = "";

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public String getReden() {
        return reden;
    }

    public void setReden(String reden) {
        this.reden = reden;
    }

    public static NietBeschikbareDagFormDto leeg() {
        return new NietBeschikbareDagFormDto();
    }

    public static NietBeschikbareDagFormDto vanDto(ChangeNietBeschikbareDagDto dto) {
        NietBeschikbareDagFormDto form = new NietBeschikbareDagFormDto();
        form.setDatum(dto.datum());
        form.setReden(dto.reden());
        return form;
    }
}
