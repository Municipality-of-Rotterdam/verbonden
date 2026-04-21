package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DossierSamenvattingDto(
        UUID id,
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort,
        BigDecimal prijs,
        LocalDateTime datumTijdHuwelijk,
        String huwelijksLocatie,
        boolean gegevensBevestigd,
        boolean getuigenBevestigd,
        List<String> extras
) implements Serializable {
}
