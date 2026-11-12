package nl.rotterdam.verbonden.remote_local.features.mock_betalen;

import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslag;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagMelding;
import nl.rotterdam.verbonden.core.online_betalen.BetaalUitslagVerwerker;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
class MockBetaalproviderImpl implements MockBetaalprovider {

    private final MockOnlineBetaalService mockOnlineBetaalService;
    private final BetaalUitslagVerwerker verwerker;

    MockBetaalproviderImpl(MockOnlineBetaalService mockOnlineBetaalService, BetaalUitslagVerwerker verwerker) {
        this.mockOnlineBetaalService = mockOnlineBetaalService;
        this.verwerker = verwerker;
    }

    @Override
    public String rondBetalingAf(String betalingskenmerk, BetaalUitslag uitslag, Duration vertraging) {
        String retourUrl = mockOnlineBetaalService.findRetourUrl(betalingskenmerk)
                .orElseThrow(() -> new IllegalArgumentException("Onbekend betalingskenmerk: " + betalingskenmerk));
        BetaalUitslagMelding melding = new BetaalUitslagMelding(betalingskenmerk, null, null, uitslag);
        if (vertraging.isPositive()) {
            CompletableFuture.runAsync(() -> verwerker.verwerkUitslag(melding),
                    CompletableFuture.delayedExecutor(vertraging.toMillis(), TimeUnit.MILLISECONDS));
        } else {
            verwerker.verwerkUitslag(melding);
        }
        return retourUrl;
    }
}
