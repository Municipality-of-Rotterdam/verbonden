package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierDetailDto;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.verbonden.core.features.marriage_intake.domain.RegistratieType;

import java.io.Serializable;

public class CeremonieFormDto implements Serializable {

    private RegistratieType registratieType;
    private CeremonieSoort ceremonieSoort;

    public static CeremonieFormDto vanDto(DossierDetailDto dto) {
        CeremonieFormDto form = new CeremonieFormDto();
        form.setRegistratieType(dto.registratieType());
        form.setCeremonieSoort(dto.ceremonieSoort());
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
}
