package nl.rotterdam.verbonden.remote_local.features.mock_betalen.ui;

import nl.rotterdam.verbonden.core.burger_common.BurgerBasePage;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslag;
import nl.rotterdam.verbonden.remote_local.features.mock_betalen.MockBetaalprovider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.time.Duration;

/**
 * Mock van de betaalpagina van een betaalprovider. Hiermee test je lokaal een gelukte, mislukte of vertraagde
 * betaling.
 */
public class MockBetaalPage extends BurgerBasePage {

    public static final String KENMERK = "kenmerk";

    @SpringBean
    private MockBetaalprovider mockBetaalprovider;

    private String kenmerk = "";

    @Override
    protected IModel<String> getTitleModel() {
        return Model.of("Mock-betaalpagina - Gemeente Rotterdam");
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        kenmerk = getPageParameters().get(KENMERK).toString("");
        pageBody.add(
                new Label("kenmerk", kenmerk),
                new UitslagLink("gelukt", BetaalUitslag.GELUKT, Duration.ZERO),
                new UitslagLink("mislukt", BetaalUitslag.MISLUKT, Duration.ZERO),
                new UitslagLink("vertraagd", BetaalUitslag.GELUKT, Duration.ofSeconds(10)),
                new UitslagLink("langzaam", BetaalUitslag.GELUKT, Duration.ofMinutes(3))
        );
    }

    private class UitslagLink extends Link<Void> {

        private final BetaalUitslag uitslag;
        private final Duration vertraging;

        UitslagLink(String id, BetaalUitslag uitslag, Duration vertraging) {
            super(id);
            this.uitslag = uitslag;
            this.vertraging = vertraging;
        }

        @Override
        public void onClick() {
            throw new RedirectToUrlException(mockBetaalprovider.rondBetalingAf(kenmerk, uitslag, vertraging));
        }
    }
}
