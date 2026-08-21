package nl.rotterdam.verbonden.remote_local;

import nl.rotterdam.verbonden.identity.PersonInfo;
import nl.rotterdam.verbonden.identity.PersonLookupService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Mock-implementatie van {@link PersonLookupService} voor lokale ontwikkeling
 * en testen. Vervangt de daadwerkelijke Haal Centraal-koppeling (of een
 * Rotterdam-eigen alternatief) die alleen in een productie-adapter-module
 * beschikbaar is.
 */
@Component
class MockPersonLookupService implements PersonLookupService {

    private static final Map<String, PersonInfo> MOCK_PERSONEN = Map.of(
            "999990007", new PersonInfo("Van Muiswinkel", "Erik Jan",
                    LocalDate.of(1984, 5, 29), "Rotterdam", "Nederlandse", "Ongehuwd"),
            "999990019", new PersonInfo("De Vries", "Sanne Maria",
                    LocalDate.of(1992, 3, 14), "Den Haag", "Nederlandse", "Ongehuwd"),
            "999990020", new PersonInfo("Jansen", "Pieter",
                    LocalDate.of(1988, 7, 22), "Groningen", "Nederlandse", "Gehuwd"),
            "999990202", new PersonInfo("Bakker", "Willem Adriaan",
                    LocalDate.of(1975, 11, 3), "Assen", "Nederlandse", "Gescheiden"),
            "999990032", new PersonInfo("Dëhlano", "Chavéliën",
                    LocalDate.of(2001, 6, 18), "Paramaribo", "Nederlandse", "Ongehuwd"),
            "999990008", new PersonInfo("Hofstede", "Jan-Diederik, deIII",
                    LocalDate.of(1999, 1, 1), "Rotterdam", "Nederlandse", "Ongehuwd")
    );

    @Override
    public Optional<PersonInfo> findByBsn(String bsn) {
        return Optional.ofNullable(MOCK_PERSONEN.get(bsn));
    }
}
