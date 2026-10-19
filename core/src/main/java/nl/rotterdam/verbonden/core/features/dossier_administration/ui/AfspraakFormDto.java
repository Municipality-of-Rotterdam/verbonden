package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierDetailDto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class AfspraakFormDto implements Serializable {

    private LocalDate datum;
    private LocalTime startTijd;

    public static AfspraakFormDto vanDto(DossierDetailDto dto) {
        AfspraakFormDto form = new AfspraakFormDto();
        if (dto.datumTijdHuwelijk() != null) {
            form.setDatum(dto.datumTijdHuwelijk().toLocalDate());
            form.setStartTijd(dto.datumTijdHuwelijk().toLocalTime());
        }
        return form;
    }

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
