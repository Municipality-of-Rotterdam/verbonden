package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierDetailDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieTaal;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;

import java.io.Serializable;

public class CeremonieFormDto implements Serializable {

    private RegistratieType registratieType;
    private CeremonieSoort ceremonieSoort;
    private CeremonieTaal taal;
    private Integer aantalBezoekers;

    public static CeremonieFormDto vanDto(DossierDetailDto dto) {
        CeremonieFormDto form = new CeremonieFormDto();
        form.setRegistratieType(dto.registratieType());
        form.setCeremonieSoort(dto.ceremonieSoort());
        form.setTaal(dto.taal());
        form.setAantalBezoekers(dto.aantalBezoekers());
        return form;
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

    public CeremonieTaal getTaal() {
        return taal;
    }

    public void setTaal(CeremonieTaal taal) {
        this.taal = taal;
    }

    public Integer getAantalBezoekers() {
        return aantalBezoekers;
    }

    public void setAantalBezoekers(Integer aantalBezoekers) {
        this.aantalBezoekers = aantalBezoekers;
    }
}
