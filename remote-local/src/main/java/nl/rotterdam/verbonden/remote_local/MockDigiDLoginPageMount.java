package nl.rotterdam.verbonden.remote_local;

import nl.rotterdam.verbonden.remote_local.features.mock_digid.ui.MockDigiDLoginPage;
import nl.rotterdam.verbonden.core.identity.BurgerLoginPageMount;
import org.apache.wicket.markup.html.WebPage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mount van de mock-DigiD-inlogpagina op {@code /inloggen}, actief zolang
 * {@code digid.mock-login.enabled} niet expliciet op {@code false} staat.
 */
@Component
@ConditionalOnProperty(name = "digid.mock-login.enabled", matchIfMissing = true)
class MockDigiDLoginPageMount implements BurgerLoginPageMount {

    @Override
    public String getPath() {
        return "/inloggen";
    }

    @Override
    public Class<? extends WebPage> getPageClass() {
        return MockDigiDLoginPage.class;
    }
}
