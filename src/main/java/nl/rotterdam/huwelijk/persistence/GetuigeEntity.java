package nl.rotterdam.huwelijk.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "huwelijksdossiers_getuigen")
public class GetuigeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    private HuwelijksDossierEntity dossier;

    @Column(name = "volgnummer", nullable = false)
    private int volgnummer;

    @Column(name = "naam", length = 500)
    private String naam;

    @Column(name = "bestand_naam", length = 500)
    private String bestandNaam;

    @Column(name = "bestand_data")
    private byte[] bestandData;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HuwelijksDossierEntity getDossier() {
        return dossier;
    }

    public void setDossier(HuwelijksDossierEntity dossier) {
        this.dossier = dossier;
    }

    public int getVolgnummer() {
        return volgnummer;
    }

    public void setVolgnummer(int volgnummer) {
        this.volgnummer = volgnummer;
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
