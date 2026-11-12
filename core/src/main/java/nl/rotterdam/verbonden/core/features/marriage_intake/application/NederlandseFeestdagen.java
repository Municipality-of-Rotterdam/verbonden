package nl.rotterdam.verbonden.core.features.marriage_intake.application;

import java.time.LocalDate;
import java.time.Month;

final class NederlandseFeestdagen {

    private NederlandseFeestdagen() {
    }

    static boolean isFeestdag(LocalDate datum) {
        int jaar = datum.getYear();
        LocalDate pasen = paasZondag(jaar);
        LocalDate koningsdag = LocalDate.of(jaar, Month.APRIL, 27);
        if (koningsdag.getDayOfWeek().getValue() == 7) {
            koningsdag = koningsdag.minusDays(1);
        }
        return datum.equals(LocalDate.of(jaar, Month.JANUARY, 1))
                || datum.equals(koningsdag)
                || datum.equals(LocalDate.of(jaar, Month.MAY, 5))
                || datum.equals(LocalDate.of(jaar, Month.DECEMBER, 25))
                || datum.equals(LocalDate.of(jaar, Month.DECEMBER, 26))
                || datum.equals(paasZondag(jaar).minusDays(2))
                || datum.equals(pasen)
                || datum.equals(pasen.plusDays(1))
                || datum.equals(pasen.plusDays(39))
                || datum.equals(pasen.plusDays(49))
                || datum.equals(pasen.plusDays(50));
    }

    private static LocalDate paasZondag(int jaar) {
        int a = jaar % 19;
        int b = jaar / 100;
        int c = jaar % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int maand = (h + l - 7 * m + 114) / 31;
        int dag = (h + l - 7 * m + 114) % 31 + 1;
        return LocalDate.of(jaar, maand, dag);
    }
}
