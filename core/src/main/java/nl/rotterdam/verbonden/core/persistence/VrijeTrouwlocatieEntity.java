package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;

@Entity
@Table(name = "vrije_trouwlocaties")
public class VrijeTrouwlocatieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false, unique = true, updatable = false)
    private HuwelijksDossierEntity dossier;

    @Column(nullable = false, length = 255)
    private String naam;

    @Column(nullable = false, length = 500)
    private String adres;

    @Column(name = "telefoonnummer", nullable = false)
    @Convert(converter = TelefoonnummerAttributeConverter.class)
    private Telefoonnummer telefoonnummer;

    @Column(name = "contactpersoon", length = 255)
    private String contactpersoon;

    @Column(name = "goedgekeurd", nullable = false)
    private boolean goedgekeurd;

    protected VrijeTrouwlocatieEntity() {
    }

    public VrijeTrouwlocatieEntity(HuwelijksDossierEntity dossier, String naam, String adres,
                                   Telefoonnummer telefoonnummer, String contactpersoon) {
        this.dossier = dossier;
        this.naam = naam;
        this.adres = adres;
        this.telefoonnummer = telefoonnummer;
        this.contactpersoon = contactpersoon;
    }

    public String getNaam() {
        return naam;
    }

    public String getAdres() {
        return adres;
    }

    public Telefoonnummer getTelefoonnummer() {
        return telefoonnummer;
    }

    public String getContactpersoon() {
        return contactpersoon;
    }

    public boolean isGoedgekeurd() {
        return goedgekeurd;
    }

    public void keurGoed() {
        goedgekeurd = true;
    }
}
