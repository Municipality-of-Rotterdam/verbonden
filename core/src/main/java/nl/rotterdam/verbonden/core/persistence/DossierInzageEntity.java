package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.InzageActie;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Eén regel in het inzagelog (AVG): welke medewerker wanneer persoonsgegevens in een dossier heeft ingezien.
 * Verwijst bewust via het UUID en niet via een relatie naar het dossier, zodat de regel blijft bestaan als het
 * dossier wordt verwijderd.
 */
@Entity
@Table(name = "dossier_inzage_log")
public class DossierInzageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dossier_uuid", nullable = false, updatable = false)
    private UUID dossierUuid;

    @Column(name = "medewerker", nullable = false, updatable = false)
    private String medewerker;

    @Enumerated(EnumType.STRING)
    @Column(name = "actie", nullable = false, updatable = false)
    private InzageActie actie;

    @Column(name = "tijdstip", nullable = false, updatable = false)
    private LocalDateTime tijdstip;

    protected DossierInzageEntity() {
    }

    public DossierInzageEntity(UUID dossierUuid, String medewerker, InzageActie actie) {
        this.dossierUuid = dossierUuid;
        this.medewerker = medewerker;
        this.actie = actie;
        this.tijdstip = LocalDateTime.now();
    }

    public String getMedewerker() {
        return medewerker;
    }
}
