package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.core.features.babs_administration.domain.PersonFullName;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "babsen")
public class BabsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Convert(converter = PersonFullNameAttributeConverter.class)
    private PersonFullName naam;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "detail_url")
    private String detailUrl;

    @Column(nullable = false)
    private boolean actief = true;

    @Column(name = "actief_vanaf")
    private LocalDate actiefVanaf;

    @Column(name = "actief_tot_en_met")
    private LocalDate actiefTotEnMet;

    @ElementCollection
    @CollectionTable(name = "babsen_beschikbare_dagen", joinColumns = @JoinColumn(name = "babs_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dag", nullable = false)
    private List<DayOfWeek> beschikbareDagen = new ArrayList<>();

    @Column(name = "aangemaakt_op", nullable = false)
    private LocalDateTime aangemaaktOp = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PersonFullName getNaam() {
        return naam;
    }

    public void setNaam(PersonFullName naam) {
        this.naam = naam;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public boolean isActief() {
        return actief;
    }

    public void setActief(boolean actief) {
        this.actief = actief;
    }

    public LocalDate getActiefVanaf() {
        return actiefVanaf;
    }

    public void setActiefVanaf(LocalDate actiefVanaf) {
        this.actiefVanaf = actiefVanaf;
    }

    public LocalDate getActiefTotEnMet() {
        return actiefTotEnMet;
    }

    public void setActiefTotEnMet(LocalDate actiefTotEnMet) {
        this.actiefTotEnMet = actiefTotEnMet;
    }

    public List<DayOfWeek> getBeschikbareDagen() {
        return beschikbareDagen;
    }

    public void setBeschikbareDagen(List<DayOfWeek> beschikbareDagen) {
        this.beschikbareDagen = beschikbareDagen != null ? beschikbareDagen : new ArrayList<>();
    }

    public LocalDateTime getAangemaaktOp() {
        return aangemaaktOp;
    }

    public void setAangemaaktOp(LocalDateTime aangemaaktOp) {
        this.aangemaaktOp = aangemaaktOp;
    }
}
