package nl.rotterdam.huwelijk.features.mock_digid.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nl.rotterdam.huwelijk.burger_common.BurgerBasePage;
import nl.rotterdam.huwelijk.features.marriage_intake.ui.MarriageIntakePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.List;

public class MockDigiDLoginPage extends BurgerBasePage {

    private record TestBurger(String bsn, String beschrijving) {}

    private static final List<TestBurger> TEST_BURGERS = List.of(
            new TestBurger("999990007", "Woont in Rotterdam, vrijgezel, man"),
            new TestBurger("999990019", "Woont in Den Haag, vrijgezel, vrouw"),
            new TestBurger("999990020", "Woont in Groningen, getrouwd"),
            new TestBurger("999990202", "Woont in Assen, gescheiden"),
            new TestBurger("999990032", "Woont in Suriname, vrijgezel. Bijzondere naam: Chavéliën Dëhlano")
    );

    @Override
    protected IModel<String> getTitleModel() {
        return Model.of("Inloggen - Gemeente Rotterdam");
    }

    public MockDigiDLoginPage() {
        pageBody.add(new ListView<TestBurger>("testBurgers", TEST_BURGERS) {
            @Override
            protected void populateItem(ListItem<TestBurger> item) {
                TestBurger burger = item.getModelObject();
                item.add(new Label("bsn", burger.bsn()));
                item.add(new Label("beschrijving", burger.beschrijving()));
                item.add(new Link<Void>("loginLink") {
                    @Override
                    public void onClick() {
                        loginAs(burger.bsn());
                    }
                });
            }
        });
    }

    private void loginAs(String bsn) {
        UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.authenticated(
                bsn, null, List.of(new SimpleGrantedAuthority("ROLE_BURGER")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);

        HttpServletRequest request = (HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest();
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        setResponsePage(MarriageIntakePage.class);
    }
}
