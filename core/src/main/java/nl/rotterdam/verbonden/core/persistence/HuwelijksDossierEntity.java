package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.core.domain.BuitenlandsPersoonsnummer;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.AanmaakKanaal;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.DossierStatus;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;

import java.math.BigDecimal;
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

    @Column(name = "internationale_akte_prijs")
    private BigDecimal internationaleAktePrijs;

    @Column(name = "aangemaakt_op", nullable = false)
    private LocalDateTime aangemaaktOp = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DossierStatus status = DossierStatus.CONCEPT;

    @Column(name = "ingediend_op")
    private LocalDateTime ingediendOp;

    @Enumerated(EnumType.STRING)
    @Column(name = "kanaal", nullable = false, updatable = false)
    private AanmaakKanaal kanaal;

    /**
     * De medewerker die het dossier aan de balie of in een videogesprek aanmaakte; {@code null} bij
     * {@link AanmaakKanaal#ONLINE}.
     */
    @Column(name = "aangemaakt_door", updatable = false)
    private String aangemaaktDoor;

    protected HuwelijksDossierEntity() {
    }

    public HuwelijksDossierEntity(AanmaakKanaal kanaal, String aangemaaktDoor) {
        this.kanaal = kanaal;
        this.aangemaaktDoor = aangemaaktDoor;
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public RegistratieType getRegistratieType() {
        return registratieType;
    }

    public CeremonieSoort getCeremonieSoort() {
        return ceremonieSoort;
    }

    public TrouwlocatieEntity getLocatie() {
        return locatie;
    }

    public List<HuwelijksDossiersPartnerEntity> getPartners() {
        return partners;
    }

    /**
     * Voegt een partner toe die zich online met DigiD heeft geïdentificeerd; de eerste partner krijgt
     * volgorde 1, de tweede volgorde 2.
     *
     * @throws IllegalStateException wanneer het dossier al twee partners heeft
     */
    public void voegPartnerToe(BurgerServiceNummer bsn) {
        voegToe(bsn, null, null);
    }

    /**
     * Voegt een partner met BSN toe van wie een medewerker de identiteit heeft gecontroleerd.
     *
     * @throws IllegalStateException wanneer het dossier al twee partners heeft
     */
    public void voegPartnerToe(BurgerServiceNummer bsn, String identiteitGecontroleerdDoor) {
        voegToe(bsn, null, identiteitGecontroleerdDoor);
    }

    /**
     * Voegt een partner zonder BSN toe, van wie een medewerker de identiteit heeft gecontroleerd. De
     * persoonsgegevens komen dan niet uit de BRP: de aanroeper legt ze vast op de teruggegeven partner.
     *
     * @throws IllegalStateException wanneer het dossier al twee partners heeft, of nog geen partner met BSN
     */
    public HuwelijksDossiersPartnerEntity voegPartnerZonderBsnToe(BuitenlandsPersoonsnummer persoonsnummer,
                                                                  String identiteitGecontroleerdDoor) {
        if (partners.isEmpty()) {
            throw new IllegalStateException("De eerste partner van dossier " + uuid + " moet een BSN hebben");
        }
        return voegToe(null, persoonsnummer, identiteitGecontroleerdDoor);
    }

    private HuwelijksDossiersPartnerEntity voegToe(BurgerServiceNummer bsn, BuitenlandsPersoonsnummer persoonsnummer,
                                                   String identiteitGecontroleerdDoor) {
        if (partners.size() >= 2) {
            throw new IllegalStateException("Dossier " + uuid + " heeft al twee partners");
        }
        HuwelijksDossiersPartnerEntity partner = new HuwelijksDossiersPartnerEntity(
                this, partners.size() + 1, bsn, persoonsnummer, identiteitGecontroleerdDoor);
        partners.add(partner);
        return partner;
    }

    /**
     * Wijzigt het soort registratie en de ceremonie. Muziek kan alleen bij een grote ceremonie.
     */
    public void wijzigCeremonie(RegistratieType registratieType, CeremonieSoort ceremonieSoort,
                                TrouwlocatieEntity locatie) {
        this.registratieType = registratieType;
        this.ceremonieSoort = ceremonieSoort;
        this.locatie = locatie;
        if (ceremonieSoort != CeremonieSoort.GROOT) {
            this.muziek = false;
        }
    }

    /**
     * Wijzigt de extra's. Muziek kan alleen bij een grote ceremonie, een internationale akte alleen bij een
     * huwelijk; zo'n keuze wordt anders genegeerd.
     */
    public void wijzigExtras(boolean ringenUitwisselen, boolean muziek, TrouwboekjeEntity trouwboekje,
                             boolean internationaleAkte) {
        this.ringenUitwisselen = ringenUitwisselen;
        this.muziek = ceremonieSoort == CeremonieSoort.GROOT && muziek;
        this.trouwboekje = trouwboekje;
        this.internationaleAkte = registratieType == RegistratieType.HUWELIJK && internationaleAkte;
    }

    public boolean isRingenUitwisselen() {
        return ringenUitwisselen;
    }

    public boolean isMuziek() {
        return muziek;
    }

    public TrouwboekjeEntity getTrouwboekje() {
        return trouwboekje;
    }

    public boolean isInternationaleAkte() {
        return internationaleAkte;
    }

    public BigDecimal getInternationaleAktePrijs() {
        return internationaleAktePrijs;
    }

    public void setInternationaleAktePrijs(BigDecimal internationaleAktePrijs) {
        this.internationaleAktePrijs = internationaleAktePrijs;
    }

    public LocalDateTime getAangemaaktOp() {
        return aangemaaktOp;
    }

    public void setAangemaaktOp(LocalDateTime aangemaaktOp) {
        this.aangemaaktOp = aangemaaktOp;
    }

    public AanmaakKanaal getKanaal() {
        return kanaal;
    }

    public String getAangemaaktDoor() {
        return aangemaaktDoor;
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
