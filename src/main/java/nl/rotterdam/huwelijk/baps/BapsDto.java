package nl.rotterdam.huwelijk.baps;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BapsDto(
        Long id,
        String naam,
        String fotoUrl,
        String hobbies,
        String beschrijving,
        boolean actief,
        LocalDate actiefVanaf,
        LocalDate actiefTotEnMet,
        List<DayOfWeek> beschikbareDagen,
        LocalDateTime aangemaaktOp
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Lege DTO voor nieuwe BAPS-invoer. */
    public static BapsDto leeg() {
        return new BapsDto(null, "", null, null, null, true, null, null, List.of(), null);
    }

    /** Geeft een nieuwe instantie terug met alleen het veld {@code actief} gewijzigd. */
    public BapsDto withActief(boolean actief) {
        return new BapsDto(id, naam, fotoUrl, hobbies, beschrijving,
                actief, actiefVanaf, actiefTotEnMet, beschikbareDagen, aangemaaktOp);
    }
}
