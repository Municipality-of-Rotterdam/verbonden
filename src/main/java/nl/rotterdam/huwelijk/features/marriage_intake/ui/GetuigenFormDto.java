package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import java.io.Serializable;

public class GetuigenFormDto implements Serializable {

    private String naam1;
    private String naam2;
    private String naam3;
    private String naam4;

    public String getNaam1() {
        return naam1;
    }

    public void setNaam1(String naam1) {
        this.naam1 = naam1;
    }

    public String getNaam2() {
        return naam2;
    }

    public void setNaam2(String naam2) {
        this.naam2 = naam2;
    }

    public String getNaam3() {
        return naam3;
    }

    public void setNaam3(String naam3) {
        this.naam3 = naam3;
    }

    public String getNaam4() {
        return naam4;
    }

    public void setNaam4(String naam4) {
        this.naam4 = naam4;
    }

    public static GetuigenFormDto vanGetuigen(java.util.List<nl.rotterdam.huwelijk.features.marriage_intake.domain.GetuigeDto> getuigen) {
        GetuigenFormDto form = new GetuigenFormDto();
        for (nl.rotterdam.huwelijk.features.marriage_intake.domain.GetuigeDto g : getuigen) {
            switch (g.volgnummer()) {
                case 1 -> form.setNaam1(g.naam());
                case 2 -> form.setNaam2(g.naam());
                case 3 -> form.setNaam3(g.naam());
                case 4 -> form.setNaam4(g.naam());
            }
        }
        return form;
    }
}
