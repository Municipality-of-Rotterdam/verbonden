package nl.rotterdam.huwelijk.features.marriage_type_administration.ui;

import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.ChangeMarriageTypeDto;
import nl.rotterdam.huwelijk.domain.MarriageType;

import java.io.Serializable;
import java.math.BigDecimal;

public class MarriageTypeFormDto implements Serializable {

    private MarriageType soort = null;
    private String titel = "";
    private String tekst = "";
    private BigDecimal prijs = null;
    private String url = "";

    public MarriageType getSoort() { return soort; }
    public void setSoort(MarriageType soort) { this.soort = soort; }

    public String getTitel() { return titel; }
    public void setTitel(String titel) { this.titel = titel; }

    public String getTekst() { return tekst; }
    public void setTekst(String tekst) { this.tekst = tekst; }

    public BigDecimal getPrijs() { return prijs; }
    public void setPrijs(BigDecimal prijs) { this.prijs = prijs; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public static MarriageTypeFormDto leeg() {
        return new MarriageTypeFormDto();
    }

    public static MarriageTypeFormDto vanDto(ChangeMarriageTypeDto dto) {
        MarriageTypeFormDto form = new MarriageTypeFormDto();
        form.setSoort(dto.soort());
        form.setTitel(dto.titel());
        form.setTekst(dto.tekst());
        form.setPrijs(dto.prijs());
        form.setUrl(dto.url());
        return form;
    }
}
