package nl.rotterdam.verbonden.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.features.marriage_intake.domain.RegistratieType;

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
    private ExtraEntity trouwboekje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internationale_akte_id")
    private ExtraEntity internationaleAkte;

    @Column(name = "aangemaakt_op", nullable = false)
    private LocalDateTime aangemaaktOp = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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

    public ExtraEntity getTrouwboekje() {
        return trouwboekje;
    }

    public void setTrouwboekje(ExtraEntity trouwboekje) {
        this.trouwboekje = trouwboekje;
    }

    public ExtraEntity getInternationaleAkte() {
        return internationaleAkte;
    }

    public void setInternationaleAkte(ExtraEntity internationaleAkte) {
        this.internationaleAkte = internationaleAkte;
    }

    public LocalDateTime getAangemaaktOp() {
        return aangemaaktOp;
    }

    public void setAangemaaktOp(LocalDateTime aangemaaktOp) {
        this.aangemaaktOp = aangemaaktOp;
    }
}
