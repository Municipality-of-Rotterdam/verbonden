package nl.rotterdam.huwelijk.features.location_administration.ui;

import nl.rotterdam.huwelijk.features.location_administration.domain.ChangeBeschikbaarheidDto;
import nl.rotterdam.huwelijk.features.location_administration.domain.HuwelijksType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class BeschikbaarheidFormDto implements Serializable {

    private HuwelijksType huwelijkstype = HuwelijksType.REGULIER;
    private DayOfWeek dagVanDeWeek = DayOfWeek.MONDAY;
    private LocalTime startTijd = null;
    private LocalTime eindTijd = null;
    private int duurInMinuten = 30;
    private BigDecimal prijs = BigDecimal.ZERO;
    private LocalDate ingangsdatum = LocalDate.of(2026, 1, 1);
    private LocalDate einddatum = LocalDate.of(2026, 12, 31);

    public HuwelijksType getHuwelijkstype() {
        return huwelijkstype;
    }

    public void setHuwelijkstype(HuwelijksType huwelijkstype) {
        this.huwelijkstype = huwelijkstype;
    }

    public DayOfWeek getDagVanDeWeek() {
        return dagVanDeWeek;
    }

    public void setDagVanDeWeek(DayOfWeek dagVanDeWeek) {
        this.dagVanDeWeek = dagVanDeWeek;
    }

    public LocalTime getStartTijd() {
        return startTijd;
    }

    public void setStartTijd(LocalTime startTijd) {
        this.startTijd = startTijd;
    }

    public LocalTime getEindTijd() {
        return eindTijd;
    }

    public void setEindTijd(LocalTime eindTijd) {
        this.eindTijd = eindTijd;
    }

    public int getDuurInMinuten() {
        return duurInMinuten;
    }

    public void setDuurInMinuten(int duurInMinuten) {
        this.duurInMinuten = duurInMinuten;
    }

    public BigDecimal getPrijs() {
        return prijs;
    }

    public void setPrijs(BigDecimal prijs) {
        this.prijs = prijs;
    }

    public LocalDate getIngangsdatum() {
        return ingangsdatum;
    }

    public void setIngangsdatum(LocalDate ingangsdatum) {
        this.ingangsdatum = ingangsdatum;
    }

    public LocalDate getEinddatum() {
        return einddatum;
    }

    public void setEinddatum(LocalDate einddatum) {
        this.einddatum = einddatum;
    }

    public static BeschikbaarheidFormDto leeg() {
        return new BeschikbaarheidFormDto();
    }

    public static BeschikbaarheidFormDto vanDto(ChangeBeschikbaarheidDto dto) {
        BeschikbaarheidFormDto form = new BeschikbaarheidFormDto();
        form.setHuwelijkstype(dto.huwelijkstype());
        form.setDagVanDeWeek(dto.dagVanDeWeek());
        form.setStartTijd(dto.startTijd());
        form.setEindTijd(dto.eindTijd());
        form.setDuurInMinuten(dto.duurInMinuten());
        form.setPrijs(dto.prijs());
        form.setIngangsdatum(dto.ingangsdatum());
        form.setEinddatum(dto.einddatum());
        return form;
    }
}
