package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public record DossierSamenvattingDto(
        long id,
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort,
        LocalDate datumHuwelijk,
        String huwelijksLocatie,
        boolean gegevensBevestigd,
        boolean getuigenBevestigd,
        List<String> extras
) implements Serializable {
}
