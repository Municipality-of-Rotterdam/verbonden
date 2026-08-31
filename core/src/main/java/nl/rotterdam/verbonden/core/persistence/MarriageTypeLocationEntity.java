package nl.rotterdam.verbonden.core.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "huwelijkstypen_locaties")
public class MarriageTypeLocationEntity {

    @Id
    @Column(name = "marriage_type_id")
    private Long marriageTypeId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "marriage_type_id")
    private MarriageTypeEntity marriageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locatie_id", nullable = false)
    private TrouwlocatieEntity locatie;

    public Long getMarriageTypeId() {
        return marriageTypeId;
    }

    public void setMarriageTypeId(Long marriageTypeId) {
        this.marriageTypeId = marriageTypeId;
    }

    public MarriageTypeEntity getMarriageType() {
        return marriageType;
    }

    public void setMarriageType(MarriageTypeEntity marriageType) {
        this.marriageType = marriageType;
    }

    public TrouwlocatieEntity getLocatie() {
        return locatie;
    }

    public void setLocatie(TrouwlocatieEntity locatie) {
        this.locatie = locatie;
    }
}
