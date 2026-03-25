package nl.rotterdam.huwelijk.features.marriage_intake.domain;

import java.io.Serializable;

public record DossierSamenvattingDto(
        long id,
        RegistratieType registratieType,
        CeremonieSoort ceremonieSoort
) implements Serializable {
}
