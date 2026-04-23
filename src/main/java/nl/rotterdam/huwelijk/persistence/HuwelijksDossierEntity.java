package nl.rotterdam.huwelijk.persistence;

import jakarta.persistence.*;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.RegistratieType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "huwelijksdossiers")
public class HuwelijksDossierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @PrePersist
    private void generateUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "registratie_type", nullable = false)
    private RegistratieType registratieType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ceremonie_soort", nullable = false)
    private CeremonieSoort ceremonieSoort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locatie_id")
    private TrouwlocatieEntity locatie;

    @Column(name = "bsn1", length = 10)
    private String bsn1;

    @Column(name = "bsn2", length = 10)
    private String bsn2;

    @Column(name = "gekozen_achternaam_bsn1")
    private String gekozenAchternaamBsn1;

    @Column(name = "gekozen_achternaam_bsn2")
    private String gekozenAchternaamBsn2;

    @Column(name = "telefoonnummer_bsn1", length = 50)
    private String telefoonnummerBsn1;

    @Column(name = "emailadres_bsn1", length = 255)
    private String emailadresBsn1;

    @Column(name = "telefoonnummer_bsn2", length = 50)
    private String telefoonnummerBsn2;

    @Column(name = "emailadres_bsn2", length = 255)
    private String emailadresBsn2;

    @Column(name = "aangemaakt_op", nullable = false)
    private LocalDateTime aangemaaktOp = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public RegistratieType getRegistratieType() {
        return registratieType;
    }

    public void setRegistratieType(RegistratieType registratieType) {
        this.registratieType = registratieType;
    }

    public CeremonieSoort getCeremonieSoort() {
        return ceremonieSoort;
    }

    public void setCeremonieSoort(CeremonieSoort ceremonieSoort) {
        this.ceremonieSoort = ceremonieSoort;
    }

    public TrouwlocatieEntity getLocatie() {
        return locatie;
    }

    public void setLocatie(TrouwlocatieEntity locatie) {
        this.locatie = locatie;
    }

    public LocalDateTime getAangemaaktOp() {
        return aangemaaktOp;
    }

    public void setAangemaaktOp(LocalDateTime aangemaaktOp) {
        this.aangemaaktOp = aangemaaktOp;
    }

    public String getBsn1() {
        return bsn1;
    }

    public void setBsn1(String bsn1) {
        this.bsn1 = bsn1;
    }

    public String getBsn2() {
        return bsn2;
    }

    public void setBsn2(String bsn2) {
        this.bsn2 = bsn2;
    }

    public String getGekozenAchternaamBsn1() {
        return gekozenAchternaamBsn1;
    }

    public void setGekozenAchternaamBsn1(String gekozenAchternaamBsn1) {
        this.gekozenAchternaamBsn1 = gekozenAchternaamBsn1;
    }

    public String getGekozenAchternaamBsn2() {
        return gekozenAchternaamBsn2;
    }

    public void setGekozenAchternaamBsn2(String gekozenAchternaamBsn2) {
        this.gekozenAchternaamBsn2 = gekozenAchternaamBsn2;
    }

    public String getTelefoonnummerBsn1() {
        return telefoonnummerBsn1;
    }

    public void setTelefoonnummerBsn1(String telefoonnummerBsn1) {
        this.telefoonnummerBsn1 = telefoonnummerBsn1;
    }

    public String getEmailadresBsn1() {
        return emailadresBsn1;
    }

    public void setEmailadresBsn1(String emailadresBsn1) {
        this.emailadresBsn1 = emailadresBsn1;
    }

    public String getTelefoonnummerBsn2() {
        return telefoonnummerBsn2;
    }

    public void setTelefoonnummerBsn2(String telefoonnummerBsn2) {
        this.telefoonnummerBsn2 = telefoonnummerBsn2;
    }

    public String getEmailadresBsn2() {
        return emailadresBsn2;
    }

    public void setEmailadresBsn2(String emailadresBsn2) {
        this.emailadresBsn2 = emailadresBsn2;
    }
}
