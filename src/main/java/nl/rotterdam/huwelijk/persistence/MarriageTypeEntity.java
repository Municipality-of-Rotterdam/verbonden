package nl.rotterdam.huwelijk.persistence;

import jakarta.persistence.*;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "marriage_type")
public class MarriageTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String tekst;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prijs;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private CeremonieSoort soort;

    @Column(name = "aangemaakt_op", nullable = false)
    private LocalDateTime aangemaaktOp = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getTekst() {
        return tekst;
    }

    public void setTekst(String tekst) {
        this.tekst = tekst;
    }

    public BigDecimal getPrijs() {
        return prijs;
    }

    public void setPrijs(BigDecimal prijs) {
        this.prijs = prijs;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public CeremonieSoort getSoort() {
        return soort;
    }

    public void setSoort(CeremonieSoort soort) {
        this.soort = soort;
    }

    public LocalDateTime getAangemaaktOp() {
        return aangemaaktOp;
    }

    public void setAangemaaktOp(LocalDateTime aangemaaktOp) {
        this.aangemaaktOp = aangemaaktOp;
    }
}
