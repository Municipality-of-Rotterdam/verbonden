package nl.rotterdam.huwelijk.features.marriage_type_administration.ui;

import nl.rotterdam.huwelijk.features.location_administration.domain.ListLocatieDto;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.CeremonieSoort;
import nl.rotterdam.huwelijk.features.marriage_type_administration.domain.ChangeMarriageTypeDto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class MarriageTypeFormDto implements Serializable {

    private CeremonieSoort soort = null;
    private String titel = "";
    private String tekst = "";
    private BigDecimal prijs = null;
    private String url = "";
    private ListLocatieDto locatie = null;

    public CeremonieSoort getSoort() { return soort; }
    public void setSoort(CeremonieSoort soort) { this.soort = soort; }

    public String getTitel() { return titel; }
    public void setTitel(String titel) { this.titel = titel; }

    public String getTekst() { return tekst; }
    public void setTekst(String tekst) { this.tekst = tekst; }

    public BigDecimal getPrijs() { return prijs; }
    public void setPrijs(BigDecimal prijs) { this.prijs = prijs; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public ListLocatieDto getLocatie() { return locatie; }
    public void setLocatie(ListLocatieDto locatie) { this.locatie = locatie; }

    public static MarriageTypeFormDto leeg() {
        return new MarriageTypeFormDto();
    }

    public static MarriageTypeFormDto vanDto(ChangeMarriageTypeDto dto, List<ListLocatieDto> alleLocaties) {
        MarriageTypeFormDto form = new MarriageTypeFormDto();
        form.setSoort(dto.soort());
        form.setTitel(dto.titel());
        form.setTekst(dto.tekst());
        form.setPrijs(dto.prijs());
        form.setUrl(dto.url());
        if (dto.locatieId() != null) {
            alleLocaties.stream()
                    .filter(l -> l.id() == dto.locatieId().longValue())
                    .findFirst()
                    .ifPresent(form::setLocatie);
        }
        return form;
    }
}
