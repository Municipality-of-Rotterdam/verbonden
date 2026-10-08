package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.domain.BuitenlandsPersoonsnummer;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.PersoonsgegevensDto;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Persoonsnummer en persoonsgegevens van een partner zonder BSN, zoals de medewerker ze van het paspoort
 * overneemt.
 */
public class PersoonsgegevensFormDto implements Serializable {

    private BuitenlandsPersoonsnummer persoonsnummer;
    private String achternaam;
    private String voornamen;
    private LocalDate geboortedatum;
    private String geboorteplaats;
    private String nationaliteit;
    private String burgerlijkeStaat;

    public PersoonsgegevensDto naarDto() {
        return new PersoonsgegevensDto(achternaam, voornamen, geboortedatum, geboorteplaats, nationaliteit,
                burgerlijkeStaat);
    }

    public BuitenlandsPersoonsnummer getPersoonsnummer() {
        return persoonsnummer;
    }

    public void setPersoonsnummer(BuitenlandsPersoonsnummer persoonsnummer) {
        this.persoonsnummer = persoonsnummer;
    }

    public String getAchternaam() {
        return achternaam;
    }

    public void setAchternaam(String achternaam) {
        this.achternaam = achternaam;
    }

    public String getVoornamen() {
        return voornamen;
    }

    public void setVoornamen(String voornamen) {
        this.voornamen = voornamen;
    }

    public LocalDate getGeboortedatum() {
        return geboortedatum;
    }

    public void setGeboortedatum(LocalDate geboortedatum) {
        this.geboortedatum = geboortedatum;
    }

    public String getGeboorteplaats() {
        return geboorteplaats;
    }

    public void setGeboorteplaats(String geboorteplaats) {
        this.geboorteplaats = geboorteplaats;
    }

    public String getNationaliteit() {
        return nationaliteit;
    }

    public void setNationaliteit(String nationaliteit) {
        this.nationaliteit = nationaliteit;
    }

    public String getBurgerlijkeStaat() {
        return burgerlijkeStaat;
    }

    public void setBurgerlijkeStaat(String burgerlijkeStaat) {
        this.burgerlijkeStaat = burgerlijkeStaat;
    }
}
