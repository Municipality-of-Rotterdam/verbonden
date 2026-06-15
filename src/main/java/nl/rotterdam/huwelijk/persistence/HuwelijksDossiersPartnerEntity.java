package nl.rotterdam.huwelijk.persistence;

import jakarta.persistence.*;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.Emailadres;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.Telefoonnummer;

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

    @Column(name = "pasfoto")
    private byte[] pasfoto;

    @Column(name = "pasfoto_content_type", length = 100)
    private String pasfotoContentType;

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

    public byte[] getPasfoto() {
        return pasfoto;
    }

    public void setPasfoto(byte[] pasfoto) {
        this.pasfoto = pasfoto;
    }

    public String getPasfotoContentType() {
        return pasfotoContentType;
    }

    public void setPasfotoContentType(String pasfotoContentType) {
        this.pasfotoContentType = pasfotoContentType;
    }
}
