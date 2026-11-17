package nl.rotterdam.verbonden.remote_local.features.mock_betalen;

import nl.rotterdam.verbonden.core.identity.AdapterPageMount;
import nl.rotterdam.verbonden.remote_local.features.mock_betalen.ui.MockBetaalPage;
import org.apache.wicket.markup.html.WebPage;
import org.springframework.stereotype.Component;

@Component
public class MockBetaalPageMount implements AdapterPageMount {

    @Override
    public String getPath() {
        return "/mock-betalen";
    }

    @Override
    public Class<? extends WebPage> getPageClass() {
        return MockBetaalPage.class;
    }
}
