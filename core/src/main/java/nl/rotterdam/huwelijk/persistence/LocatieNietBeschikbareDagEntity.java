package nl.rotterdam.huwelijk.persistence;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trouwlocaties_niet_beschikbare_dagen")
public class LocatieNietBeschikbareDagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locatie_id", nullable = false)
    private TrouwlocatieEntity locatie;

    @Column(nullable = false)
    private LocalDate datum;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reden;

    @Column(name = "laatste_wijzig_datum", nullable = false)
    private LocalDateTime laatsteWijzigDatum;

    @Column(nullable = false)
    private String userid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getReden() {
        return reden;
    }

    public void setReden(String reden) {
        this.reden = reden;
    }

    public LocalDateTime getLaatsteWijzigDatum() {
        return laatsteWijzigDatum;
    }

    public void setLaatsteWijzigDatum(LocalDateTime laatsteWijzigDatum) {
        this.laatsteWijzigDatum = laatsteWijzigDatum;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
