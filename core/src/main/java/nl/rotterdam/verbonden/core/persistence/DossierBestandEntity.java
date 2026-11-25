package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierBijlageType;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierBestandType;

import java.time.LocalDateTime;

/**
 * Een bestand dat een medewerker aan een dossier heeft toegevoegd, bijvoorbeeld een scan van een paspoort.
 */
@Entity
@Table(name = "dossier_bestanden")
public class DossierBestandEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false, updatable = false)
    private HuwelijksDossierEntity dossier;

    @Column(name = "bestandsnaam", nullable = false, updatable = false)
    private String bestandsnaam;

    @Enumerated(EnumType.STRING)
    @Column(name = "bestand_type", nullable = false, updatable = false)
    private DossierBestandType bestandType;

    @Enumerated(EnumType.STRING)
    @Column(name = "bijlage_type", nullable = false, updatable = false)
    private DossierBijlageType bijlageType;

    @Column(name = "opmerking", length = 255, updatable = false)
    private String opmerking;

    @Column(name = "grootte", nullable = false, updatable = false)
    private long grootte;

    @Column(name = "inhoud", nullable = false, updatable = false)
    private byte[] inhoud;

    @Column(name = "toegevoegd_door", nullable = false, updatable = false)
    private String toegevoegdDoor;

    @Column(name = "toegevoegd_op", nullable = false, updatable = false)
    private LocalDateTime toegevoegdOp;

    protected DossierBestandEntity() {
    }

    public DossierBestandEntity(HuwelijksDossierEntity dossier, String bestandsnaam, DossierBestandType bestandType,
                                DossierBijlageType bijlageType, String opmerking, byte[] inhoud, String toegevoegdDoor) {
        this.dossier = dossier;
        this.bestandsnaam = bestandsnaam;
        this.bestandType = bestandType;
        this.bijlageType = bijlageType;
        this.opmerking = opmerking;
        this.grootte = inhoud.length;
        this.inhoud = inhoud;
        this.toegevoegdDoor = toegevoegdDoor;
        this.toegevoegdOp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getBestandsnaam() {
        return bestandsnaam;
    }

    public DossierBestandType getBestandType() {
        return bestandType;
    }

    public DossierBijlageType getBijlageType() {
        return bijlageType;
    }

    public String getOpmerking() {
        return opmerking;
    }

    public byte[] getInhoud() {
        return inhoud;
    }
}
