package nl.rotterdam.huwelijk.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trouwlocatie")
public class TrouwlocatieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String naam;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "aangemaakt_op", nullable = false)
    private LocalDateTime aangemaaktOp = LocalDateTime.now();

    @OneToMany(mappedBy = "locatie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocatieBeschikbaarheidEntity> beschikbaarheden = new ArrayList<>();

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
}
