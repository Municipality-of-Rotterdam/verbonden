package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import nl.rotterdam.verbonden.core.domain.Bedrag;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

final class VrijeLocatieTarief {

    private VrijeLocatieTarief() {
    }

    static Bedrag bepaal(LocalDate datum, Bedrag tijdvakTarief, List<Bedrag> dagTarieven) {
        if (datum.getDayOfWeek() == DayOfWeek.SUNDAY || NederlandseFeestdagen.isFeestdag(datum)) {
            return dagTarieven.stream().max(Bedrag::compareTo).orElse(tijdvakTarief);
        }
        return tijdvakTarief;
    }
}
