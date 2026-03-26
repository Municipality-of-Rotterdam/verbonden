package nl.rotterdam.huwelijk.persistence;

import jakarta.persistence.*;
import nl.rotterdam.huwelijk.features.location_administration.domain.HuwelijksType;

@Entity
@Table(name = "huwelijkstype_locatie")
public class HuwelijkstypeLocatieEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HuwelijksType huwelijkstype;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locatie_id", nullable = false)
    private TrouwlocatieEntity locatie;

    public HuwelijksType getHuwelijkstype() {
        return huwelijkstype;
    }

    public void setHuwelijkstype(HuwelijksType huwelijkstype) {
        this.huwelijkstype = huwelijkstype;
    }

    public TrouwlocatieEntity getLocatie() {
        return locatie;
    }

    public void setLocatie(TrouwlocatieEntity locatie) {
        this.locatie = locatie;
    }
}
