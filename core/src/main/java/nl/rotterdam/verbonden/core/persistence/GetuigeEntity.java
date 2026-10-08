package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;

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
