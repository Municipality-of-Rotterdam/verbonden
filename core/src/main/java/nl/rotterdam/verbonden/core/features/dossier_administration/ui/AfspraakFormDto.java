package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class AfspraakFormDto implements Serializable {

    private LocalDate datum;
    private LocalTime startTijd;

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public LocalTime getStartTijd() {
        return startTijd;
    }

    public void setStartTijd(LocalTime startTijd) {
        this.startTijd = startTijd;
    }
}
