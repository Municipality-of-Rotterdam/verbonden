package nl.rotterdam.verbonden.core.integration_test;

import nl.rotterdam.verbonden.core.domain.BurgerServiceNummer;
import nl.rotterdam.verbonden.core.identity.PersonInfo;
import nl.rotterdam.verbonden.core.identity.PersonLookupService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test-only stand-in for {@link PersonLookupService}, needed because core's integration
 * tests boot a full Spring context (via {@link VerbondenIntegrationTest}) and
 * {@code MarriageIntakeServiceImpl} has a required dependency on this SPI. Core cannot
 * depend on remote-local's mock (that would be a circular module dependency), so this
 * duplicates the same mock BSN dataset as remote-local's {@code MockPersonLookupService}
 * — several of the moved tests hardcode those exact BSNs.
 *
 * <p>Daarnaast faalt het opzoeken voor {@link #ONBEREIKBAAR_BSN} (alsof de BRP onbereikbaar is),
 * en wordt per BSN geteld hoe vaak er is opgezocht.
 */
@Component
public class TestPersonLookupService implements PersonLookupService {

    public static final String ONBEREIKBAAR_BSN = "999990044";

    private static final Map<String, PersonInfo> MOCK_PERSONEN = Map.of(
            "999990007", new PersonInfo("Van Muiswinkel", "Erik Jan", "E.J. van Muiswinkel",
                    LocalDate.of(1984, 5, 29), "Rotterdam", "Nederlandse", "Ongehuwd"),
            "999990019", new PersonInfo("De Vries", "Sanne Maria", "S.M. de Vries",
                    LocalDate.of(1992, 3, 14), "Den Haag", "Nederlandse", "Ongehuwd"),
            "999990020", new PersonInfo("Jansen", "Pieter", "P. Jansen",
                    LocalDate.of(1988, 7, 22), "Groningen", "Nederlandse", "Gehuwd"),
            "999990202", new PersonInfo("Bakker", "Willem Adriaan", "W.A. Bakker",
                    LocalDate.of(1975, 11, 3), "Assen", "Nederlandse", "Gescheiden"),
            "999990032", new PersonInfo("Dëhlano", "Chavéliën", "C. Dëhlano",
                    LocalDate.of(2001, 6, 18), "Paramaribo", "Nederlandse", "Ongehuwd"),
            "999990008", new PersonInfo("Hofstede", "Jan-Diederik, deIII", "J. Hofstede",
                    LocalDate.of(1999, 1, 1), "Rotterdam", "Nederlandse", "Ongehuwd")
    );

    private final Map<String, AtomicInteger> lookupCounts = new ConcurrentHashMap<>();

    @Override
    public Optional<PersonInfo> findByBsn(BurgerServiceNummer bsn) {
        lookupCounts.computeIfAbsent(bsn.getValue(), _ -> new AtomicInteger()).incrementAndGet();
        if (ONBEREIKBAAR_BSN.equals(bsn.getValue())) {
            throw new IllegalStateException("BRP onbereikbaar");
        }
        return Optional.ofNullable(MOCK_PERSONEN.get(bsn.getValue()));
    }

    public int getLookupCount(String bsn) {
        AtomicInteger count = lookupCounts.get(bsn);
        return count != null ? count.get() : 0;
    }
}
