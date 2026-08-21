package nl.rotterdam.verbonden.features.extra_administration.ui;

import nl.rotterdam.verbonden.features.extra_administration.domain.ChangeExtraDto;
import nl.rotterdam.verbonden.features.extra_administration.domain.ExtraType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ExtraFormDto implements Serializable {

    private ExtraType type;
    private String naam;
    private String omschrijving;
    private String afbeelding;
    private BigDecimal prijs;
    private LocalDate startdatum;
    private LocalDate einddatum;

    public static ExtraFormDto leeg() {
        return new ExtraFormDto();
    }

    public static ExtraFormDto vanDto(ChangeExtraDto dto) {
        ExtraFormDto form = new ExtraFormDto();
        form.setType(dto.type());
        form.setNaam(dto.naam());
        form.setOmschrijving(dto.omschrijving());
        form.setAfbeelding(dto.afbeelding());
        form.setPrijs(dto.prijs());
        form.setStartdatum(dto.startdatum());
        form.setEinddatum(dto.einddatum());
        return form;
    }

    public ExtraType getType() {
        return type;
    }

    public void setType(ExtraType type) {
        this.type = type;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getOmschrijving() {
        return omschrijving;
    }

    public void setOmschrijving(String omschrijving) {
        this.omschrijving = omschrijving;
    }

    public String getAfbeelding() {
        return afbeelding;
    }

    public void setAfbeelding(String afbeelding) {
        this.afbeelding = afbeelding;
    }

    public BigDecimal getPrijs() {
        return prijs;
    }

    public void setPrijs(BigDecimal prijs) {
        this.prijs = prijs;
    }

    public LocalDate getStartdatum() {
        return startdatum;
    }

    public void setStartdatum(LocalDate startdatum) {
        this.startdatum = startdatum;
    }

    public LocalDate getEinddatum() {
        return einddatum;
    }

    public void setEinddatum(LocalDate einddatum) {
        this.einddatum = einddatum;
    }
}
