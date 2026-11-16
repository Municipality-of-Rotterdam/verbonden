package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.time.LocalDateTime;

/**
 * Een bedrag dat het paar na het indienen nog moet betalen, bijvoorbeeld voor een bijbesteld trouwboekje. Betaald
 * zodra een {@link BetalingEntity} ervoor gelukt is.
 */
@Entity
@Table(name = "betaalmutaties")
public class BetaalmutatieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false, updatable = false)
    private HuwelijksDossierEntity dossier;

    @Column(name = "omschrijving", nullable = false, updatable = false)
    private String omschrijving;

    @Convert(converter = BedragAttributeConverter.class)
    @Column(name = "bedrag", nullable = false, updatable = false)
    private Bedrag bedrag;

    /**
     * {@code true} als Verbonden de mutatie aanmaakte omdat het dossier duurder werd; die zit dan al in de totaalprijs.
     * {@code false} voor een handmatige mutatie, die bovenop de totaalprijs komt.
     */
    @Column(name = "prijswijziging", nullable = false, updatable = false)
    private boolean prijswijziging;

    @Column(name = "aangemaakt_door", nullable = false, updatable = false)
    private String aangemaaktDoor;

    @Column(name = "aangemaakt_op", nullable = false, updatable = false)
    private LocalDateTime aangemaaktOp;

    protected BetaalmutatieEntity() {
    }

    BetaalmutatieEntity(HuwelijksDossierEntity dossier, String omschrijving, Bedrag bedrag, boolean prijswijziging,
                        String aangemaaktDoor, LocalDateTime aangemaaktOp) {
        this.dossier = dossier;
        this.omschrijving = omschrijving;
        this.bedrag = bedrag;
        this.prijswijziging = prijswijziging;
        this.aangemaaktDoor = aangemaaktDoor;
        this.aangemaaktOp = aangemaaktOp;
    }

    public Long getId() {
        return id;
    }

    public HuwelijksDossierEntity getDossier() {
        return dossier;
    }

    public String getOmschrijving() {
        return omschrijving;
    }

    public Bedrag getBedrag() {
        return bedrag;
    }

    public boolean isPrijswijziging() {
        return prijswijziging;
    }

    public String getAangemaaktDoor() {
        return aangemaaktDoor;
    }

    public LocalDateTime getAangemaaktOp() {
        return aangemaaktOp;
    }
}
