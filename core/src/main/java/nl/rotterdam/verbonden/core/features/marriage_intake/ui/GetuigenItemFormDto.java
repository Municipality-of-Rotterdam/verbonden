package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.babs_administration.domain.PersonFullName;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.GetuigeDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GetuigenItemFormDto implements Serializable {

    private final int volgnummer;
    private PersonFullName naam;

    public GetuigenItemFormDto(int volgnummer) {
        this.volgnummer = volgnummer;
    }

    public int getVolgnummer() { return volgnummer; }

    public PersonFullName getNaam() { return naam; }
    public void setNaam(PersonFullName naam) { this.naam = naam; }

    public static List<GetuigenItemFormDto> vanGetuigen(int maxGetuigen, List<GetuigeDto> bestaande) {
        List<GetuigenItemFormDto> items = new ArrayList<>();
        for (int i = 1; i <= maxGetuigen; i++) {
            GetuigenItemFormDto item = new GetuigenItemFormDto(i);
            for (GetuigeDto g : bestaande) {
                if (g.volgnummer() == i) {
                    item.setNaam(toPersonFullName(g.naam()));
                    break;
                }
            }
            items.add(item);
        }
        return items;
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
