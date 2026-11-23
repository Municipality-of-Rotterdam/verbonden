package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "afspraken")
public class AfspraakEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false, updatable = false)
    private HuwelijksDossierEntity dossier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locatie_id", nullable = false)
    private TrouwlocatieEntity locatie;

    @Column(nullable = false)
    private LocalDate datum;

    @Column(name = "start_tijd", nullable = false)
    private LocalTime startTijd;

    @Column(name = "eind_tijd", nullable = false)
    private LocalTime eindTijd;

    @Column(name = "locatie_prijs", precision = 10, scale = 2)
    @Convert(converter = BedragAttributeConverter.class)
    private Bedrag locatiePrijs;

    protected AfspraakEntity() {
    }

    public AfspraakEntity(HuwelijksDossierEntity dossier) {
        this.dossier = dossier;
    }

    public Long getId() {
        return id;
    }

    public HuwelijksDossierEntity getDossier() {
        return dossier;
    }

    public TrouwlocatieEntity getLocatie() {
        return locatie;
    }

    public void setLocatie(TrouwlocatieEntity locatie) {
        this.locatie = locatie;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public LocalTime getStartTijd() {
        return startTijd;
    }

    public void setStartTijd(LocalTime startTijd) {
        this.startTijd = startTijd;
    }

    public LocalTime getEindTijd() {
        return eindTijd;
    }

    public void setEindTijd(LocalTime eindTijd) {
        this.eindTijd = eindTijd;
    }

    public Bedrag getLocatiePrijs() {
        return locatiePrijs;
    }

    public void setLocatiePrijs(Bedrag locatiePrijs) {
        this.locatiePrijs = locatiePrijs;
    }
}
