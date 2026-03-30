package nl.rotterdam.huwelijk.features.mock_digid.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nl.rotterdam.huwelijk.burger_common.BurgerBasePage;
import nl.rotterdam.huwelijk.features.marriage_intake.ui.MarriageIntakePage;
import nl.rotterdam.nl_design_system.wicket.components.table.RdDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MockDigiDLoginPage extends BurgerBasePage {

    private record TestBurger(String bsn, String beschrijving) implements Serializable {}

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
        pageBody.add(buildTestBurgersTable());
    }

    private RdDataTable<TestBurger, String> buildTestBurgersTable() {
        List<IColumn<TestBurger, String>> columns = new ArrayList<>();

        columns.add(new AbstractColumn<>(Model.of("Burgerservicenummer")) {
            @Override
            public void populateItem(Item<ICellPopulator<TestBurger>> cellItem,
                                     String componentId,
                                     IModel<TestBurger> rowModel) {
                cellItem.add(new Label(componentId, rowModel.map(TestBurger::bsn)));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("Omschrijving")) {
            @Override
            public void populateItem(Item<ICellPopulator<TestBurger>> cellItem,
                                     String componentId,
                                     IModel<TestBurger> rowModel) {
                cellItem.add(new Label(componentId, rowModel.map(TestBurger::beschrijving)));
            }
        });

        columns.add(new AbstractColumn<>(Model.of("")) {
            @Override
            public void populateItem(Item<ICellPopulator<TestBurger>> cellItem,
                                     String componentId,
                                     IModel<TestBurger> rowModel) {
                cellItem.add(new LoginFragment(componentId, rowModel));
            }
        });

        return new RdDataTable<>("testBurgersTable", columns,
                new ListDataProvider<>(TEST_BURGERS), TEST_BURGERS.size());
    }

    private class LoginFragment extends Fragment {
        LoginFragment(String id, IModel<TestBurger> model) {
            super(id, "loginFragment", MockDigiDLoginPage.this);
            add(new Link<Void>("loginLink") {
                @Override
                public void onClick() {
                    loginAs(model.getObject().bsn());
                }
            });
        }
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
