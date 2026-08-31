package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.SaveExtrasDto;

import java.io.Serializable;

public class ExtrasFormDto implements Serializable {

    private boolean ringenUitwisselen;
    private boolean muziek;
    private Long trouwboekjeId;
    private Long internationaleAkteId;

    public static ExtrasFormDto vanSelecties(SaveExtrasDto dto) {
        ExtrasFormDto form = new ExtrasFormDto();
        form.setRingenUitwisselen(dto.ringenUitwisselen());
        form.setMuziek(dto.muziek());
        form.setTrouwboekjeId(dto.trouwboekjeId());
        form.setInternationaleAkteId(dto.internationaleAkteId());
        return form;
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

    public Long getTrouwboekjeId() {
        return trouwboekjeId;
    }

    public void setTrouwboekjeId(Long trouwboekjeId) {
        this.trouwboekjeId = trouwboekjeId;
    }

    public Long getInternationaleAkteId() {
        return internationaleAkteId;
    }

    public void setInternationaleAkteId(Long internationaleAkteId) {
        this.internationaleAkteId = internationaleAkteId;
    }
}
