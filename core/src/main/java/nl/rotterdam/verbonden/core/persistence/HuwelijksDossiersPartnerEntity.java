package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.Emailadres;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.Telefoonnummer;

@Entity
@Table(name = "huwelijksdossiers_partners")
public class HuwelijksDossiersPartnerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    private HuwelijksDossierEntity dossier;

    @Column(name = "volgorde", nullable = false)
    private int volgorde;

    @Column(name = "bsn", length = 10, nullable = false)
    private String bsn;

    @Column(name = "gekozen_achternaam")
    private String gekozenAchternaam;

    @Convert(converter = TelefoonnummerAttributeConverter.class)
    @Column(name = "telefoonnummer", length = 50)
    private Telefoonnummer telefoonnummer;

    @Convert(converter = EmailadresAttributeConverter.class)
    @Column(name = "emailadres", length = 255)
    private Emailadres emailadres;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HuwelijksDossierEntity getDossier() {
        return dossier;
    }

    public void setDossier(HuwelijksDossierEntity dossier) {
        this.dossier = dossier;
    }

    public int getVolgorde() {
        return volgorde;
    }

    public void setVolgorde(int volgorde) {
        this.volgorde = volgorde;
    }

    public String getBsn() {
        return bsn;
    }

    public void setBsn(String bsn) {
        this.bsn = bsn;
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
}
