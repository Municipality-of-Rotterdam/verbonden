package nl.rotterdam.huwelijk.features.baps_administration.ui;

import nl.rotterdam.huwelijk.features.baps_administration.domain.ChangeBapsDto;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Mutable form model object for BAPS forms.
 * Fields are public — no getters/setters needed.
 * Use {@link org.apache.wicket.model.LambdaModel} with direct field lambdas for each field and
 * {@link org.apache.wicket.model.util.ListModel} for the {@code beschikbareDagen} list.
 */
public class BapsFormDto implements Serializable {

    public String naam = "";
    public String fotoUrl = "";
    public String hobbies = "";
    public String beschrijving = "";
    public List<DayOfWeek> beschikbareDagen = new ArrayList<>();
    public boolean actief = true;
    public LocalDate actiefVanaf = null;
    public LocalDate actiefTotEnMet = null;

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
        form.actiefVanaf = dto.actiefVanaf();
        form.actiefTotEnMet = dto.actiefTotEnMet();
        return form;
    }
}
