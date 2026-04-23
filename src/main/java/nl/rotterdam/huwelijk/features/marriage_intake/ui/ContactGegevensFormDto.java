package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import java.io.Serializable;

public class ContactGegevensFormDto implements Serializable {

    private String telefoonnummer;
    private String emailadres;

    public String getTelefoonnummer() {
        return telefoonnummer;
    }

    public void setTelefoonnummer(String telefoonnummer) {
        this.telefoonnummer = telefoonnummer;
    }

    public String getEmailadres() {
        return emailadres;
    }

    public void setEmailadres(String emailadres) {
        this.emailadres = emailadres;
    }
}
