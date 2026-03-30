package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DossierSamenvattingDto(
        UUID id,
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort,
        LocalDate datumHuwelijk,
        String huwelijksLocatie,
        boolean gegevensBevestigd,
        boolean getuigenBevestigd,
        List<String> extras
) implements Serializable {
}
