package nl.rotterdam.verbonden.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trouwlocaties")
public class TrouwlocatieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String naam;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(columnDefinition = "TEXT")
    private String omschrijving;

    @Column(name = "detail_url")
    private String detailUrl;

    @Column(name = "aangemaakt_op", nullable = false)
    private LocalDateTime aangemaaktOp = LocalDateTime.now();

    @OneToMany(mappedBy = "locatie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocatieBeschikbaarheidEntity> beschikbaarheden = new ArrayList<>();

    @OneToMany(mappedBy = "locatie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocatieNietBeschikbareDagEntity> nietBeschikbareDagen = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getOmschrijving() {
        return omschrijving;
    }

    public void setOmschrijving(String omschrijving) {
        this.omschrijving = omschrijving;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public LocalDateTime getAangemaaktOp() {
        return aangemaaktOp;
    }

    public void setAangemaaktOp(LocalDateTime aangemaaktOp) {
        this.aangemaaktOp = aangemaaktOp;
    }

    public List<LocatieBeschikbaarheidEntity> getBeschikbaarheden() {
        return beschikbaarheden;
    }

    public void setBeschikbaarheden(List<LocatieBeschikbaarheidEntity> beschikbaarheden) {
        this.beschikbaarheden = beschikbaarheden != null ? beschikbaarheden : new ArrayList<>();
    }

    public List<LocatieNietBeschikbareDagEntity> getNietBeschikbareDagen() {
        return nietBeschikbareDagen;
    }

    public void setNietBeschikbareDagen(List<LocatieNietBeschikbareDagEntity> nietBeschikbareDagen) {
        this.nietBeschikbareDagen = nietBeschikbareDagen != null ? nietBeschikbareDagen : new ArrayList<>();
    }
}
