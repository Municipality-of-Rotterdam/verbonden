package nl.rotterdam.huwelijk.baps;

import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "baps")
public class BapsEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    @Column(name = "beschikbare_dagen", columnDefinition = "TEXT")
    private String beschikbareDagen;

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

    public String getBeschikbareDagen() {
        return beschikbareDagen;
    }

    public void setBeschikbareDagen(String beschikbareDagen) {
        this.beschikbareDagen = beschikbareDagen;
    }

    public LocalDateTime getAangemaaktOp() {
        return aangemaaktOp;
    }

    public void setAangemaaktOp(LocalDateTime aangemaaktOp) {
        this.aangemaaktOp = aangemaaktOp;
    }
}
