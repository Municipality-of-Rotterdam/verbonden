package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.core.domain.BuitenlandsPersoonsnummer;
import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.domain.Emailadres;
import nl.rotterdam.verbonden.core.domain.Telefoonnummer;
import org.hibernate.annotations.OptimisticLock;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "huwelijksdossiers_partners")
public class HuwelijksDossiersPartnerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false, updatable = false)
    private HuwelijksDossierEntity dossier;

    @Column(name = "volgorde", nullable = false, updatable = false)
    private int volgorde;

    /**
     * {@code null} voor een partner zonder BSN; die heeft dan een {@link #buitenlandsPersoonsnummer}.
     */
    @Convert(converter = BurgerServiceNummerAttributeConverter.class)
    @Column(name = "bsn", length = 10, updatable = false)
    private BurgerServiceNummer bsn;

    @Convert(converter = BuitenlandsPersoonsnummerAttributeConverter.class)
    @Column(name = "buitenlands_persoonsnummer", length = 50)
    private BuitenlandsPersoonsnummer buitenlandsPersoonsnummer;

    // Persoonsgegevens van een partner zonder BSN; voor een partner met BSN komen ze uit de BRP.

    @Column(name = "achternaam")
    private String achternaam;

    @Column(name = "voornamen")
    private String voornamen;

    @Column(name = "geboortedatum")
    private LocalDate geboortedatum;

    @Column(name = "geboorteplaats")
    private String geboorteplaats;

    @Column(name = "nationaliteit")
    private String nationaliteit;

    @Column(name = "burgerlijke_staat")
    private String burgerlijkeStaat;

    /**
     * De medewerker die de identiteit aan de balie of in een videogesprek heeft gecontroleerd; {@code null}
     * wanneer de partner zich online met DigiD heeft geïdentificeerd.
     */
    @Column(name = "identiteit_gecontroleerd_door", updatable = false)
    private String identiteitGecontroleerdDoor;

    @Column(name = "identiteit_gecontroleerd_op", updatable = false)
    private LocalDateTime identiteitGecontroleerdOp;

    /**
     * Valt buiten de optimistic lock: de naamkeuze is een expliciete, volledige keuze, en zou anders een
     * gelijktijdige wijziging van de contactgegevens onterecht als conflict laten zien.
     */
    @OptimisticLock(excluded = true)
    @Column(name = "gekozen_achternaam")
    private String gekozenAchternaam;

    @Convert(converter = TelefoonnummerAttributeConverter.class)
    @Column(name = "telefoonnummer", length = 50)
    private Telefoonnummer telefoonnummer;

    @Convert(converter = EmailadresAttributeConverter.class)
    @Column(name = "emailadres", length = 255)
    private Emailadres emailadres;

    @Version
    @Column(name = "versie", nullable = false)
    private long versie;

    protected HuwelijksDossiersPartnerEntity() {
    }

    /**
     * Alleen via {@link HuwelijksDossierEntity#voegPartnerToe}, dat de volgorde bepaalt.
     */
    HuwelijksDossiersPartnerEntity(HuwelijksDossierEntity dossier, int volgorde, BurgerServiceNummer bsn,
                                   BuitenlandsPersoonsnummer buitenlandsPersoonsnummer,
                                   String identiteitGecontroleerdDoor) {
        this.dossier = dossier;
        this.volgorde = volgorde;
        this.bsn = bsn;
        this.buitenlandsPersoonsnummer = buitenlandsPersoonsnummer;
        this.identiteitGecontroleerdDoor = identiteitGecontroleerdDoor;
        this.identiteitGecontroleerdOp = identiteitGecontroleerdDoor != null ? LocalDateTime.now() : null;
    }

    public Long getId() {
        return id;
    }

    public HuwelijksDossierEntity getDossier() {
        return dossier;
    }

    public int getVolgorde() {
        return volgorde;
    }

    public BurgerServiceNummer getBsn() {
        return bsn;
    }

    public BuitenlandsPersoonsnummer getBuitenlandsPersoonsnummer() {
        return buitenlandsPersoonsnummer;
    }

    public void setBuitenlandsPersoonsnummer(BuitenlandsPersoonsnummer buitenlandsPersoonsnummer) {
        this.buitenlandsPersoonsnummer = buitenlandsPersoonsnummer;
    }

    public String getAchternaam() {
        return achternaam;
    }

    public void setAchternaam(String achternaam) {
        this.achternaam = achternaam;
    }

    public String getVoornamen() {
        return voornamen;
    }

    public void setVoornamen(String voornamen) {
        this.voornamen = voornamen;
    }

    public LocalDate getGeboortedatum() {
        return geboortedatum;
    }

    public void setGeboortedatum(LocalDate geboortedatum) {
        this.geboortedatum = geboortedatum;
    }

    public String getGeboorteplaats() {
        return geboorteplaats;
    }

    public void setGeboorteplaats(String geboorteplaats) {
        this.geboorteplaats = geboorteplaats;
    }

    public String getNationaliteit() {
        return nationaliteit;
    }

    public void setNationaliteit(String nationaliteit) {
        this.nationaliteit = nationaliteit;
    }

    public String getBurgerlijkeStaat() {
        return burgerlijkeStaat;
    }

    public void setBurgerlijkeStaat(String burgerlijkeStaat) {
        this.burgerlijkeStaat = burgerlijkeStaat;
    }

    public String getIdentiteitGecontroleerdDoor() {
        return identiteitGecontroleerdDoor;
    }

    public LocalDateTime getIdentiteitGecontroleerdOp() {
        return identiteitGecontroleerdOp;
    }

    public String getGekozenAchternaam() {
        return gekozenAchternaam;
    }

    public void setGekozenAchternaam(String gekozenAchternaam) {
        this.gekozenAchternaam = gekozenAchternaam;
    }

    public Telefoonnummer getTelefoonnummer() {
        return telefoonnummer;
    }

    public void setTelefoonnummer(Telefoonnummer telefoonnummer) {
        this.telefoonnummer = telefoonnummer;
    }

    public Emailadres getEmailadres() {
        return emailadres;
    }

    public void setEmailadres(Emailadres emailadres) {
        this.emailadres = emailadres;
    }

    public long getVersie() {
        return versie;
    }
}
