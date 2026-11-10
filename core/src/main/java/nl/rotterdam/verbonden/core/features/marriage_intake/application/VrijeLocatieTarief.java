package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

final class VrijeLocatieTarief {

    private static final Bedrag HOOG = Bedrag.of("2168.60");
    private static final Bedrag DOORDEWEEKS_DAG = Bedrag.of("770.90");
    private static final Bedrag AVOND = Bedrag.of("1084.30");

    private VrijeLocatieTarief() {
    }

    static Bedrag bepaal(LocalDate datum, LocalTime tijd) {
        if (NederlandseFeestdagen.isFeestdag(datum)) {
            return HOOG;
        }
        DayOfWeek dag = datum.getDayOfWeek();
        if (dag == DayOfWeek.SUNDAY) {
            return HOOG;
        }
        boolean tussenNegenEnNegen = !tijd.isBefore(LocalTime.of(9, 0))
                && tijd.isBefore(LocalTime.of(21, 0));
        if (dag == DayOfWeek.SATURDAY) {
            return tussenNegenEnNegen ? AVOND : HOOG;
        }
        if (tijd.isBefore(LocalTime.of(9, 0)) || !tijd.isBefore(LocalTime.of(21, 0))) {
            return HOOG;
        }
        return tijd.isBefore(LocalTime.of(17, 0)) ? DOORDEWEEKS_DAG : AVOND;
    }
}
