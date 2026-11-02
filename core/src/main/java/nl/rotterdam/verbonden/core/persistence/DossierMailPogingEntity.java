package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailPogingStatus;

import java.time.LocalDateTime;

/**
 * Eén poging om een {@link DossierMailEntity e-mail} te verzenden.
 */
@Entity
@Table(name = "dossier_mail_pogingen")
public class DossierMailPogingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mail_id", nullable = false, updatable = false)
    private DossierMailEntity mail;

    @Column(name = "volgnummer", nullable = false, updatable = false)
    private int volgnummer;

    @Column(name = "tijdstip", nullable = false, updatable = false)
    private LocalDateTime tijdstip;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, updatable = false)
    private DossierMailPogingStatus status;

    @Column(name = "foutmelding", updatable = false, columnDefinition = "TEXT")
    private String foutmelding;

    protected DossierMailPogingEntity() {
    }

    DossierMailPogingEntity(DossierMailEntity mail, int volgnummer, LocalDateTime tijdstip,
                            DossierMailPogingStatus status, String foutmelding) {
        this.mail = mail;
        this.volgnummer = volgnummer;
        this.tijdstip = tijdstip;
        this.status = status;
        this.foutmelding = foutmelding;
    }

    public int getVolgnummer() {
        return volgnummer;
    }

    public LocalDateTime getTijdstip() {
        return tijdstip;
    }

    public DossierMailPogingStatus getStatus() {
        return status;
    }

    public String getFoutmelding() {
        return foutmelding;
    }
}
