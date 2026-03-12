package nl.rotterdam.huwelijk.features.baps_administration.ui;

import nl.rotterdam.huwelijk.features.baps_administration.domain.ChangeBapsDto;
import nl.rotterdam.huwelijk.features.baps_administration.domain.PersonFullName;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Mutable form model object for BAPS forms.
 * Fields are private with getters and setters, so Wicket's {@link org.apache.wicket.model.LambdaModel}
 * can bind to them via method references (e.g. {@code BapsFormDto::getNaam}, {@code BapsFormDto::setNaam}).
 * Java lambdas cannot directly point to public fields the way method references can point to methods,
 * so getters and setters are necessary for clean LambdaModel bindings.
 */
public class BapsFormDto implements Serializable {

    private PersonFullName naam = null;
    private String fotoUrl = "";
    private String hobbies = "";
    private String beschrijving = "";
    private List<DayOfWeek> beschikbareDagen = new ArrayList<>();
    private boolean actief = true;
    private LocalDate actiefVanaf = null;
    private LocalDate actiefTotEnMet = null;

    public PersonFullName getNaam() { return naam; }
    public void setNaam(PersonFullName naam) { this.naam = naam; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getHobbies() { return hobbies; }
    public void setHobbies(String hobbies) { this.hobbies = hobbies; }

    public String getBeschrijving() { return beschrijving; }
    public void setBeschrijving(String beschrijving) { this.beschrijving = beschrijving; }

    public List<DayOfWeek> getBeschikbareDagen() { return beschikbareDagen; }
    public void setBeschikbareDagen(List<DayOfWeek> beschikbareDagen) { this.beschikbareDagen = beschikbareDagen; }

    public boolean isActief() { return actief; }
    public void setActief(boolean actief) { this.actief = actief; }

    public LocalDate getActiefVanaf() { return actiefVanaf; }
    public void setActiefVanaf(LocalDate actiefVanaf) { this.actiefVanaf = actiefVanaf; }

    public LocalDate getActiefTotEnMet() { return actiefTotEnMet; }
    public void setActiefTotEnMet(LocalDate actiefTotEnMet) { this.actiefTotEnMet = actiefTotEnMet; }

    /** Lege instantie voor een nieuw aan te maken BAPS. */
    public static BapsFormDto leeg() {
        return new BapsFormDto();
    }

    /** Vult een instantie met de waarden uit een bestaande {@link ChangeBapsDto}. */
    public static BapsFormDto vanDto(ChangeBapsDto dto) {
        BapsFormDto form = new BapsFormDto();
        form.setNaam(dto.naam());
        form.setFotoUrl(dto.fotoUrl());
        form.setHobbies(dto.hobbies());
        form.setBeschrijving(dto.beschrijving());
        form.setBeschikbareDagen(dto.beschikbareDagen() != null
                ? new ArrayList<>(dto.beschikbareDagen()) : new ArrayList<>());
        form.setActief(dto.actief());
        form.setActiefVanaf(dto.actiefVanaf());
        form.setActiefTotEnMet(dto.actiefTotEnMet());
        return form;
    }
}
