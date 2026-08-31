package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.core.features.location_administration.domain.HuwelijksType;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "trouwlocaties_beschikbaarheden")
public class LocatieBeschikbaarheidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locatie_id", nullable = false)
    private TrouwlocatieEntity locatie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HuwelijksType huwelijkstype;

    @Enumerated(EnumType.STRING)
    @Column(name = "dag_van_de_week", nullable = false)
    private DayOfWeek dagVanDeWeek;

    @Column(name = "start_tijd", nullable = false)
    private LocalTime startTijd;

    @Column(name = "eind_tijd", nullable = false)
    private LocalTime eindTijd;

    @Column(name = "duur_in_minuten", nullable = false)
    private int duurInMinuten;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prijs;

    @Column(nullable = false)
    private LocalDate ingangsdatum;

    @Column(nullable = false)
    private LocalDate einddatum;

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

    public HuwelijksType getHuwelijkstype() {
        return huwelijkstype;
    }

    public void setHuwelijkstype(HuwelijksType huwelijkstype) {
        this.huwelijkstype = huwelijkstype;
    }

    public DayOfWeek getDagVanDeWeek() {
        return dagVanDeWeek;
    }

    public void setDagVanDeWeek(DayOfWeek dagVanDeWeek) {
        this.dagVanDeWeek = dagVanDeWeek;
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

    public int getDuurInMinuten() {
        return duurInMinuten;
    }

    public void setDuurInMinuten(int duurInMinuten) {
        this.duurInMinuten = duurInMinuten;
    }

    public BigDecimal getPrijs() {
        return prijs;
    }

    public void setPrijs(BigDecimal prijs) {
        this.prijs = prijs;
    }

    public LocalDate getIngangsdatum() {
        return ingangsdatum;
    }

    public void setIngangsdatum(LocalDate ingangsdatum) {
        this.ingangsdatum = ingangsdatum;
    }

    public LocalDate getEinddatum() {
        return einddatum;
    }

    public void setEinddatum(LocalDate einddatum) {
        this.einddatum = einddatum;
    }
}
