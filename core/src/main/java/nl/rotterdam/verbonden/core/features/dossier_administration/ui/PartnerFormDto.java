package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;

import java.io.Serializable;

public class PartnerFormDto implements Serializable {

    private long versie;
    private String gekozenAchternaam;
    private Telefoonnummer telefoonnummer;
    private Emailadres emailadres;
    private final PersoonsgegevensFormDto persoonsgegevens = new PersoonsgegevensFormDto();

    public static PartnerFormDto vanDto(PartnerGegevensDto dto) {
        PartnerFormDto form = new PartnerFormDto();
        form.setVersie(dto.versie());
        form.setGekozenAchternaam(dto.gekozenAchternaam());
        form.setTelefoonnummer(dto.telefoonnummer());
        form.setEmailadres(dto.emailadres());
        PersoonsgegevensFormDto p = form.getPersoonsgegevens();
        p.setPersoonsnummer(dto.buitenlandsPersoonsnummer());
        p.setAchternaam(dto.achternaam());
        p.setVoornamen(dto.voornamen());
        p.setGeboortedatum(dto.geboortedatum());
        p.setGeboorteplaats(dto.geboorteplaats());
        p.setNationaliteit(dto.nationaliteit());
        p.setBurgerlijkeStaat(dto.burgerlijkeStaat());
        return form;
    }

    public long getVersie() {
        return versie;
    }

    public void setVersie(long versie) {
        this.versie = versie;
    }

    public String getGekozenAchternaam() {
        return gekozenAchternaam;
    }

    public void setGekozenAchternaam(String gekozenAchternaam) {
        this.gekozenAchternaam = gekozenAchternaam;
    }

    public Telefoonnummer getTelefoonnummer() {
        return telefoonnummer;
    }

    public void setTelefoonnummer(Telefoonnummer telefoonnummer) {
        this.telefoonnummer = telefoonnummer;
    }

    public Emailadres getEmailadres() {
        return emailadres;
    }

    public void setEmailadres(Emailadres emailadres) {
        this.emailadres = emailadres;
    }

    /** Alleen van belang voor een partner zonder BSN. */
    public PersoonsgegevensFormDto getPersoonsgegevens() {
        return persoonsgegevens;
    }
}
