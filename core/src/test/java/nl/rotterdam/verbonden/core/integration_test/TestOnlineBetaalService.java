package nl.rotterdam.verbonden.core.integration_test;

import nl.rotterdam.verbonden.core.online_betalen.BetalingGestart;
import nl.rotterdam.verbonden.core.online_betalen.OnlineBetaalService;
import nl.rotterdam.verbonden.core.online_betalen.OnlineBetalenNietBeschikbaarException;
import nl.rotterdam.verbonden.core.online_betalen.StartBetalingOpdracht;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test-only stand-in voor {@link OnlineBetaalService}, om dezelfde reden als {@link TestPersonLookupService}.
 * Onthoudt de opdrachten, en kan doen alsof de betaalprovider niet bereikbaar is. De Spring-context wordt gedeeld
 * tussen testklassen: roep {@link #reset()} aan in een {@code @AfterEach}.
 */
@Component
public class TestOnlineBetaalService implements OnlineBetaalService {

    public static final String BETAALPAGINA = "https://betaalprovider.test/betalen/";

    private final List<StartBetalingOpdracht> opdrachten = new CopyOnWriteArrayList<>();
    private volatile boolean nietBeschikbaar;

    @Override
    public BetalingGestart startBetaling(StartBetalingOpdracht opdracht) {
        if (nietBeschikbaar) {
            throw new OnlineBetalenNietBeschikbaarException("Betaalprovider niet bereikbaar (test)", null);
        }
        opdrachten.add(opdracht);
        return new BetalingGestart(BETAALPAGINA + opdracht.betalingskenmerk(), "extern-" + opdracht.betalingskenmerk());
    }

    public StartBetalingOpdracht laatsteOpdracht() {
        return opdrachten.getLast();
    }

    public List<StartBetalingOpdracht> opdrachten() {
        return List.copyOf(opdrachten);
    }

    public void zetNietBeschikbaar() {
        nietBeschikbaar = true;
    }

    public void reset() {
        opdrachten.clear();
        nietBeschikbaar = false;
    }
}
