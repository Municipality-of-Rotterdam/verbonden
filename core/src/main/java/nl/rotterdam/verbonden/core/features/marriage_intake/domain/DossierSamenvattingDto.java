package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DossierSamenvattingDto(
        UUID id,
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort,
        Bedrag prijs,
        LocalDateTime datumTijdHuwelijk,
        String huwelijksLocatie,
        boolean gegevensBevestigd,
        boolean getuigenBevestigd,
        boolean getuigenGedeeltelijkIngevuld,
        List<SidebarExtraItemDto> extras,
        boolean extrasDoorlopen,
        int aantalGekozenAchternamen,
        boolean partnerGekoppeld,
        Bedrag totalPrijs,
        DossierStatus status,
        LocalDateTime ingediendOp,
        List<OntbrekendOnderdeel> ontbrekendeOnderdelen
) implements Serializable {

    public boolean compleet() {
        return ontbrekendeOnderdelen.isEmpty();
    }
}
