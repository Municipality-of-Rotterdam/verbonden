package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Een onveranderlijke notitie die een medewerker aan een dossier heeft toegevoegd.
 */
@Entity
@Table(name = "dossier_notities")
public class DossierNotitieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dossier_id", nullable = false, updatable = false)
    private HuwelijksDossierEntity dossier;

    @Column(name = "tekst", nullable = false, updatable = false, length = 1000)
    private String tekst;

    @Column(name = "gebruikersnaam", nullable = false, updatable = false)
    private String gebruikersnaam;

    @Column(name = "aangemaakt_op", nullable = false, updatable = false)
    private LocalDateTime aangemaaktOp;

    protected DossierNotitieEntity() {
    }

    public DossierNotitieEntity(HuwelijksDossierEntity dossier, String tekst, String gebruikersnaam,
                                LocalDateTime aangemaaktOp) {
        this.dossier = dossier;
        this.tekst = tekst;
        this.gebruikersnaam = gebruikersnaam;
        this.aangemaaktOp = aangemaaktOp;
    }

    public Long getId() {
        return id;
    }
}
