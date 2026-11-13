package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailPogingStatus;
import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailStatus;
import nl.rotterdam.verbonden.core.features.dossier_mail.domain.DossierMailType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Een e-mail die voor een dossier is verstuurd of nog moet worden verstuurd, met de inhoud zoals die is
 * verstuurd en elke verzendpoging.
 */
@Entity
@Table(name = "dossier_mails")
public class DossierMailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dossier_id", nullable = false, updatable = false)
    private HuwelijksDossierEntity dossier;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private DossierMailType type;

    @Column(name = "afzender", nullable = false, updatable = false)
    private String afzender;

    /** Het e-mailadres van de ontvanger; {@code null} als er niemand was om de e-mail naar te sturen. */
    @Column(name = "ontvanger", updatable = false, length = 320)
    private String ontvanger;

    @Column(name = "onderwerp", nullable = false, updatable = false)
    private String onderwerp;

    @Column(name = "inhoud", nullable = false, updatable = false, columnDefinition = "TEXT")
    private String inhoud;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DossierMailStatus status = DossierMailStatus.IN_WACHT;

    @Column(name = "aangemaakt_op", nullable = false, updatable = false)
    private LocalDateTime aangemaaktOp;

    @Column(name = "volgende_poging_op")
    private LocalDateTime volgendePogingOp;

    @OneToMany(mappedBy = "mail", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("volgnummer ASC")
    private List<DossierMailPogingEntity> pogingen = new ArrayList<>();

    protected DossierMailEntity() {
    }

    public DossierMailEntity(HuwelijksDossierEntity dossier, DossierMailType type, String afzender,
                             String ontvanger, String onderwerp, String inhoud, LocalDateTime aangemaaktOp) {
        this.dossier = dossier;
        this.type = type;
        this.afzender = afzender;
        this.ontvanger = ontvanger;
        this.onderwerp = onderwerp;
        this.inhoud = inhoud;
        this.aangemaaktOp = aangemaaktOp;
        this.volgendePogingOp = aangemaaktOp;
    }

    /**
     * Legt een verzendpoging vast. De volgorde van de pogingen volgt uit het aantal eerdere pogingen.
     */
    public void voegPogingToe(LocalDateTime tijdstip, DossierMailPogingStatus pogingStatus, String foutmelding) {
        pogingen.add(new DossierMailPogingEntity(this, pogingen.size() + 1, tijdstip, pogingStatus, foutmelding));
    }

    public Long getId() {
        return id;
    }

    public DossierMailType getType() {
        return type;
    }

    public String getAfzender() {
        return afzender;
    }

    public String getOntvanger() {
        return ontvanger;
    }

    public String getOnderwerp() {
        return onderwerp;
    }

    public String getInhoud() {
        return inhoud;
    }

    public DossierMailStatus getStatus() {
        return status;
    }

    public void setStatus(DossierMailStatus status) {
        this.status = status;
    }

    public LocalDateTime getAangemaaktOp() {
        return aangemaaktOp;
    }

    public LocalDateTime getVolgendePogingOp() {
        return volgendePogingOp;
    }

    public void setVolgendePogingOp(LocalDateTime volgendePogingOp) {
        this.volgendePogingOp = volgendePogingOp;
    }

    public List<DossierMailPogingEntity> getPogingen() {
        return pogingen;
    }
}
