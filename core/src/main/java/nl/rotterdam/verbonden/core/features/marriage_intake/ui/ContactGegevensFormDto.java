package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;

import java.io.Serializable;

public class ContactGegevensFormDto implements Serializable {

    private Telefoonnummer telefoonnummer;
    private Emailadres emailadres;
    private long versie;

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

    public long getVersie() {
        return versie;
    }

    public void setVersie(long versie) {
        this.versie = versie;
    }
}
