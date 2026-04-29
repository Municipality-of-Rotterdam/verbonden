package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.babs_administration.domain.PersonFullName;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.GetuigeDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.LegitimatieFileUpload;

import java.io.Serializable;
import java.util.List;

/**
 * Mutable form model object for the getuigen form.
 * Holds the current name (as a typed {@link PersonFullName}) and any previously saved
 * legitimacy-document file (as a {@link LegitimatieFileUpload}) for each of the four witnesses.
 */
public class GetuigenFormDto implements Serializable {

    private PersonFullName naam1;
    private PersonFullName naam2;
    private PersonFullName naam3;
    private PersonFullName naam4;

    private LegitimatieFileUpload bestand1;
    private LegitimatieFileUpload bestand2;
    private LegitimatieFileUpload bestand3;
    private LegitimatieFileUpload bestand4;

    public PersonFullName getNaam1() { return naam1; }
    public void setNaam1(PersonFullName naam1) { this.naam1 = naam1; }

    public PersonFullName getNaam2() { return naam2; }
    public void setNaam2(PersonFullName naam2) { this.naam2 = naam2; }

    public PersonFullName getNaam3() { return naam3; }
    public void setNaam3(PersonFullName naam3) { this.naam3 = naam3; }

    public PersonFullName getNaam4() { return naam4; }
    public void setNaam4(PersonFullName naam4) { this.naam4 = naam4; }

    public LegitimatieFileUpload getBestand1() { return bestand1; }
    public void setBestand1(LegitimatieFileUpload bestand1) { this.bestand1 = bestand1; }

    public LegitimatieFileUpload getBestand2() { return bestand2; }
    public void setBestand2(LegitimatieFileUpload bestand2) { this.bestand2 = bestand2; }

    public LegitimatieFileUpload getBestand3() { return bestand3; }
    public void setBestand3(LegitimatieFileUpload bestand3) { this.bestand3 = bestand3; }

    public LegitimatieFileUpload getBestand4() { return bestand4; }
    public void setBestand4(LegitimatieFileUpload bestand4) { this.bestand4 = bestand4; }

    /** Builds a new instance pre-populated from the given list of already-saved witnesses. */
    public static GetuigenFormDto vanGetuigen(List<GetuigeDto> getuigen) {
        GetuigenFormDto form = new GetuigenFormDto();
        for (GetuigeDto g : getuigen) {
            PersonFullName naam = toPersonFullName(g.naam());
            LegitimatieFileUpload bestand = g.bestand();
            switch (g.volgnummer()) {
                case 1 -> { form.setNaam1(naam); form.setBestand1(bestand); }
                case 2 -> { form.setNaam2(naam); form.setBestand2(bestand); }
                case 3 -> { form.setNaam3(naam); form.setBestand3(bestand); }
                case 4 -> { form.setNaam4(naam); form.setBestand4(bestand); }
            }
        }
        return form;
    }

    private static PersonFullName toPersonFullName(String naam) {
        if (naam == null || naam.isBlank()) {
            return null;
        }
        try {
            return new PersonFullName(naam);
        } catch (Exception e) {
            return null;
        }
    }
}

