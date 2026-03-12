package nl.rotterdam.huwelijk.features.baps_administration;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Mutable form model object for BAPS forms.
 * Use {@link org.apache.wicket.model.LambdaModel} for each field and
 * {@link org.apache.wicket.model.util.ListModel} for the {@code beschikbareDagen} list.
 */
public class BapsFormDto implements Serializable {

    private String naam = "";
    private String fotoUrl = "";
    private String hobbies = "";
    private String beschrijving = "";
    private List<DayOfWeek> beschikbareDagen = new ArrayList<>();
    private boolean actief = true;
    private String actiefVanaf = "";
    private String actiefTotEnMet = "";

    /** Lege instantie voor een nieuw aan te maken BAPS. */
    public static BapsFormDto leeg() {
        return new BapsFormDto();
    }

    /** Vult een instantie met de waarden uit een bestaande {@link ChangeBapsDto}. */
    public static BapsFormDto vanDto(ChangeBapsDto dto) {
        BapsFormDto form = new BapsFormDto();
        form.naam = dto.naam() != null ? dto.naam() : "";
        form.fotoUrl = dto.fotoUrl() != null ? dto.fotoUrl() : "";
        form.hobbies = dto.hobbies() != null ? dto.hobbies() : "";
        form.beschrijving = dto.beschrijving() != null ? dto.beschrijving() : "";
        form.beschikbareDagen = dto.beschikbareDagen() != null
                ? new ArrayList<>(dto.beschikbareDagen()) : new ArrayList<>();
        form.actief = dto.actief();
        form.actiefVanaf = dto.actiefVanaf() != null ? dto.actiefVanaf().toString() : ""; // ISO-8601 (YYYY-MM-DD)
        form.actiefTotEnMet = dto.actiefTotEnMet() != null ? dto.actiefTotEnMet().toString() : ""; // ISO-8601 (YYYY-MM-DD)
        return form;
    }

    public String getNaam() { return naam; }
    public void setNaam(String naam) { this.naam = naam; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getHobbies() { return hobbies; }
    public void setHobbies(String hobbies) { this.hobbies = hobbies; }

    public String getBeschrijving() { return beschrijving; }
    public void setBeschrijving(String beschrijving) { this.beschrijving = beschrijving; }

    public Collection<DayOfWeek> getBeschikbareDagen() { return beschikbareDagen; }
    public void setBeschikbareDagen(Collection<DayOfWeek> beschikbareDagen) {
        this.beschikbareDagen = beschikbareDagen != null ? List.copyOf(beschikbareDagen) : new ArrayList<>();
    }

    public boolean isActief() { return actief; }
    public void setActief(boolean actief) { this.actief = actief; }

    public String getActiefVanaf() { return actiefVanaf; }
    public void setActiefVanaf(String actiefVanaf) { this.actiefVanaf = actiefVanaf; }

    public String getActiefTotEnMet() { return actiefTotEnMet; }
    public void setActiefTotEnMet(String actiefTotEnMet) { this.actiefTotEnMet = actiefTotEnMet; }
}
