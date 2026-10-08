package nl.rotterdam.verbonden.core.features.dossier_administration.domain;

import nl.rotterdam.verbonden.core.features.marriage_intake.domain.PartnerGegevensDto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @param identiteitGecontroleerdDoor de medewerker die de identiteit controleerde, of {@code null} wanneer de
 *                                    partner zich online met DigiD heeft geïdentificeerd
 */
public record DetailPartnerDto(
        PartnerGegevensDto gegevens,
        String identiteitGecontroleerdDoor,
        LocalDateTime identiteitGecontroleerdOp
) implements Serializable {
}
