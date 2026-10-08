package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "huwelijksdossiers")
public class HuwelijksDossierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @PrePersist
    private void generateUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "registratie_type", nullable = false)
    private RegistratieType registratieType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ceremonie_soort", nullable = false)
    private CeremonieSoort ceremonieSoort;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locatie_id")
    private TrouwlocatieEntity locatie;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("volgorde ASC")
    private List<HuwelijksDossiersPartnerEntity> partners = new ArrayList<>();

    @Column(name = "ringen_uitwisselen", nullable = false)
    private boolean ringenUitwisselen = false;

    @Column(name = "muziek", nullable = false)
    private boolean muziek = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trouwboekje_id")
    private TrouwboekjeEntity trouwboekje;

    @Column(name = "internationale_akte", nullable = false)
    private boolean internationaleAkte = false;

    @Column(name = "aangemaakt_op", nullable = false)
    private LocalDateTime aangemaaktOp = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DossierStatus status = DossierStatus.CONCEPT;

    @Column(name = "ingediend_op")
    private LocalDateTime ingediendOp;

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public RegistratieType getRegistratieType() {
        return registratieType;
    }

    public void setRegistratieType(RegistratieType registratieType) {
        this.registratieType = registratieType;
    }

    public CeremonieSoort getCeremonieSoort() {
        return ceremonieSoort;
    }

    public void setCeremonieSoort(CeremonieSoort ceremonieSoort) {
        this.ceremonieSoort = ceremonieSoort;
    }

    public TrouwlocatieEntity getLocatie() {
        return locatie;
    }

    public void setLocatie(TrouwlocatieEntity locatie) {
        this.locatie = locatie;
    }

    public List<HuwelijksDossiersPartnerEntity> getPartners() {
        return partners;
    }

    /**
     * Voegt een partner toe aan het dossier; de eerste partner krijgt volgorde 1, de tweede volgorde 2.
     *
     * @throws IllegalStateException wanneer het dossier al twee partners heeft
     */
    public void voegPartnerToe(BurgerServiceNummer bsn) {
        if (partners.size() >= 2) {
            throw new IllegalStateException("Dossier " + uuid + " heeft al twee partners");
        }
        partners.add(new HuwelijksDossiersPartnerEntity(this, partners.size() + 1, bsn));
    }

    public boolean isRingenUitwisselen() {
        return ringenUitwisselen;
    }

    public void setRingenUitwisselen(boolean ringenUitwisselen) {
        this.ringenUitwisselen = ringenUitwisselen;
    }

    public boolean isMuziek() {
        return muziek;
    }

    public void setMuziek(boolean muziek) {
        this.muziek = muziek;
    }

    public TrouwboekjeEntity getTrouwboekje() {
        return trouwboekje;
    }

    public void setTrouwboekje(TrouwboekjeEntity trouwboekje) {
        this.trouwboekje = trouwboekje;
    }

    public boolean isInternationaleAkte() {
        return internationaleAkte;
    }

    public void setInternationaleAkte(boolean internationaleAkte) {
        this.internationaleAkte = internationaleAkte;
    }

    public LocalDateTime getAangemaaktOp() {
        return aangemaaktOp;
    }

    public void setAangemaaktOp(LocalDateTime aangemaaktOp) {
        this.aangemaaktOp = aangemaaktOp;
    }

    public DossierStatus getStatus() {
        return status;
    }

    public void setStatus(DossierStatus status) {
        this.status = status;
    }

    public LocalDateTime getIngediendOp() {
        return ingediendOp;
    }

    public void setIngediendOp(LocalDateTime ingediendOp) {
        this.ingediendOp = ingediendOp;
    }
}
