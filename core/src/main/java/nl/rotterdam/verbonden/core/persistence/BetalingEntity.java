package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.core.domain.Bedrag;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.BetalingStatus;
import nl.rotterdam.verbonden.core.online_betalen.BetaalSoort;

import java.time.LocalDateTime;

/**
 * Eén online betaalpoging voor een dossier.
 */
@Entity
@Table(name = "betalingen")
public class BetalingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false, updatable = false)
    private HuwelijksDossierEntity dossier;

    @Enumerated(EnumType.STRING)
    @Column(name = "soort", nullable = false, updatable = false)
    private BetaalSoort soort;

    /** De betaalmutatie waarvoor betaald wordt; alleen bij {@link BetaalSoort#MUTATIE}. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "betaalmutatie_id", updatable = false)
    private BetaalmutatieEntity betaalmutatie;

    @Column(name = "betalingskenmerk", nullable = false, unique = true, updatable = false)
    private String betalingskenmerk;

    @Convert(converter = BedragAttributeConverter.class)
    @Column(name = "bedrag", nullable = false, updatable = false)
    private Bedrag bedrag;

    @Convert(converter = BurgerServiceNummerAttributeConverter.class)
    @Column(name = "betaler_bsn", length = 10, nullable = false, updatable = false)
    private BurgerServiceNummer betalerBsn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BetalingStatus status = BetalingStatus.GESTART;

    @Column(name = "externe_referentie")
    private String externeReferentie;

    @Column(name = "gestart_op", nullable = false, updatable = false)
    private LocalDateTime gestartOp;

    @Column(name = "afgerond_op")
    private LocalDateTime afgerondOp;

    protected BetalingEntity() {
    }

    BetalingEntity(HuwelijksDossierEntity dossier, BetaalmutatieEntity betaalmutatie, String betalingskenmerk,
                   Bedrag bedrag, BurgerServiceNummer betalerBsn, LocalDateTime gestartOp) {
        this.dossier = dossier;
        this.soort = betaalmutatie == null ? BetaalSoort.REGISTRATIE : BetaalSoort.MUTATIE;
        this.betaalmutatie = betaalmutatie;
        this.betalingskenmerk = betalingskenmerk;
        this.bedrag = bedrag;
        this.betalerBsn = betalerBsn;
        this.gestartOp = gestartOp;
    }

    /**
     * Legt de uitslag vast die de betaalprovider meldt.
     */
    public void rondAf(BetalingStatus uitslag, LocalDateTime afgerondOp) {
        if (uitslag == BetalingStatus.GESTART) {
            throw new IllegalArgumentException("Een uitslag is " + BetalingStatus.GELUKT + " of " + BetalingStatus.MISLUKT);
        }
        this.status = uitslag;
        this.afgerondOp = afgerondOp;
    }

    public Long getId() {
        return id;
    }

    public HuwelijksDossierEntity getDossier() {
        return dossier;
    }

    public BetaalSoort getSoort() {
        return soort;
    }

    public BetaalmutatieEntity getBetaalmutatie() {
        return betaalmutatie;
    }

    public String getBetalingskenmerk() {
        return betalingskenmerk;
    }

    public Bedrag getBedrag() {
        return bedrag;
    }

    public BurgerServiceNummer getBetalerBsn() {
        return betalerBsn;
    }

    public BetalingStatus getStatus() {
        return status;
    }

    public String getExterneReferentie() {
        return externeReferentie;
    }

    public void setExterneReferentie(String externeReferentie) {
        this.externeReferentie = externeReferentie;
    }

    public LocalDateTime getGestartOp() {
        return gestartOp;
    }

    public LocalDateTime getAfgerondOp() {
        return afgerondOp;
    }
}
