package nl.rotterdam.huwelijk.baps;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "baps")
public class BapsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String naam;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(columnDefinition = "TEXT")
    private String hobbies;

    @Column(columnDefinition = "TEXT")
    private String beschrijving;

    @Column(nullable = false)
    private boolean actief = true;

    @Column(name = "actief_vanaf")
    private LocalDate actiefVanaf;

    @Column(name = "actief_tot_en_met")
    private LocalDate actiefTotEnMet;

    @ElementCollection
    @CollectionTable(name = "baps_beschikbare_dagen", joinColumns = @JoinColumn(name = "baps_id"))
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

    public String getHobbies() {
        return hobbies;
    }

    public void setHobbies(String hobbies) {
        this.hobbies = hobbies;
    }

    public String getBeschrijving() {
        return beschrijving;
    }

    public void setBeschrijving(String beschrijving) {
        this.beschrijving = beschrijving;
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
