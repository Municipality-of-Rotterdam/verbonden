package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;
import nl.rotterdam.verbonden.core.domain.Postcode;

import java.time.LocalDate;

@Entity
@Table(name = "huwelijksdossiers_getuigen")
public class GetuigeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false, updatable = false)
    private HuwelijksDossierEntity dossier;

    @Column(name = "volgnummer", nullable = false, updatable = false)
    private int volgnummer;

    @Column(name = "naam", length = 500)
    private String naam;

    @Column(name = "voornamen")
    private String voornamen;

    @Column(name = "achternaam")
    private String achternaam;

    @Column(name = "geboortedatum")
    private LocalDate geboortedatum;

    @Column(name = "postcode", length = Postcode.MAXIMALE_LENGTE)
    @Convert(converter = PostcodeAttributeConverter.class)
    private Postcode postcode;

    @Column(name = "woonplaats")
    private String woonplaats;

    @Column(name = "land")
    private String land;

    @Column(name = "bestand_naam", length = 500)
    private String bestandNaam;

    @Column(name = "bestand_data")
    private byte[] bestandData;

    protected GetuigeEntity() {
    }

    public GetuigeEntity(HuwelijksDossierEntity dossier, int volgnummer) {
        this.dossier = dossier;
        this.volgnummer = volgnummer;
    }

    public Long getId() {
        return id;
    }

    public HuwelijksDossierEntity getDossier() {
        return dossier;
    }

    public int getVolgnummer() {
        return volgnummer;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getVoornamen() {
        return voornamen;
    }

    public void setVoornamen(String voornamen) {
        this.voornamen = voornamen;
    }

    public String getAchternaam() {
        return achternaam;
    }

    public void setAchternaam(String achternaam) {
        this.achternaam = achternaam;
    }

    public LocalDate getGeboortedatum() {
        return geboortedatum;
    }

    public void setGeboortedatum(LocalDate geboortedatum) {
        this.geboortedatum = geboortedatum;
    }

    public Postcode getPostcode() {
        return postcode;
    }

    public void setPostcode(Postcode postcode) {
        this.postcode = postcode;
    }

    public String getWoonplaats() {
        return woonplaats;
    }

    public void setWoonplaats(String woonplaats) {
        this.woonplaats = woonplaats;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }

    public String getBestandNaam() {
        return bestandNaam;
    }

    public void setBestandNaam(String bestandNaam) {
        this.bestandNaam = bestandNaam;
    }

    public byte[] getBestandData() {
        return bestandData;
    }

    public void setBestandData(byte[] bestandData) {
        this.bestandData = bestandData;
    }
}
