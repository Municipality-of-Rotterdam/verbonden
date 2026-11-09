package nl.rotterdam.verbonden.remote_local.features.mock_betalen;

import nl.rotterdam.verbonden.core.online_betalen.BetalingGestart;
import nl.rotterdam.verbonden.core.online_betalen.OnlineBetaalService;
import nl.rotterdam.verbonden.core.online_betalen.StartBetalingOpdracht;
import nl.rotterdam.verbonden.remote_local.features.mock_betalen.ui.MockBetaalPage;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock-betaalprovider voor lokale ontwikkeling: stuurt de burger naar de {@link MockBetaalPage mock-betaalpagina}
 * in plaats van naar een echte betaalprovider.
 * <p>
 * De retour-URL wordt hier bewaard en niet in de URL van de mock-betaalpagina meegegeven, zodat die pagina geen
 * open redirect is.
 */
@Component
public class MockOnlineBetaalService implements OnlineBetaalService {

    private final Map<String, String> retourUrls = new ConcurrentHashMap<>();

    @Override
    public BetalingGestart startBetaling(StartBetalingOpdracht opdracht) {
        retourUrls.put(opdracht.betalingskenmerk(), opdracht.retourUrl());
        RequestCycle cycle = RequestCycle.get();
        String url = cycle.getUrlRenderer().renderFullUrl(Url.parse(cycle.urlFor(MockBetaalPage.class,
                new PageParameters().set(MockBetaalPage.KENMERK, opdracht.betalingskenmerk()))));
        return new BetalingGestart(url, "mock-" + opdracht.betalingskenmerk());
    }

    public Optional<String> findRetourUrl(String betalingskenmerk) {
        return Optional.ofNullable(retourUrls.get(betalingskenmerk));
    }
}
