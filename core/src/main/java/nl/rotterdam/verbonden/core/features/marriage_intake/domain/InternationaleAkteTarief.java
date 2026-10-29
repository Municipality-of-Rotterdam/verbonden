package nl.rotterdam.verbonden.core.features.marriage_intake.domain;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.time.LocalDate;

/**
 * Het tarief van de internationale huwelijksakte hangt af van de datum waarop de aanvraag wordt ingediend.
 */
public final class InternationaleAkteTarief {

    private static final LocalDate INGANGSDATUM_TARIEF_2027 = LocalDate.of(2027, 1, 1);
    private static final Bedrag TARIEF_2026 = Bedrag.of("17.80");
    private static final Bedrag TARIEF_2027 = Bedrag.of("18.30");

    private InternationaleAkteTarief() {
    }

    public static Bedrag prijsOp(LocalDate aanvraagDatum) {
        return aanvraagDatum.isBefore(INGANGSDATUM_TARIEF_2027) ? TARIEF_2026 : TARIEF_2027;
    }
}
