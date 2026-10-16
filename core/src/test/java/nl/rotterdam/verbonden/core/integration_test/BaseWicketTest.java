package nl.rotterdam.verbonden.core.integration_test;


import nl.rotterdam.verbonden.core.WicketApplication;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.visit.IVisit;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

@VerbondenIntegrationTest
public abstract class BaseWicketTest {


    @Autowired
    protected WicketApplication application;
    protected WicketTester tester;

    @BeforeEach
    public void setup() {
        tester = new WicketTester(application);
    }

    protected FormTester formTester(String formId) {
        return tester.newFormTester(pad(formId));
    }

    /** Het pad van een component op de laatst getoonde pagina, op basis van zijn wicket:id. */
    protected String pad(String id) {
        Component component = tester.getLastRenderedPage().visitChildren(
                (Component c, IVisit<Component> visit) -> {
                    if (c.getId().equals(id)) {
                        visit.stop(c);
                    }
                });
        return component.getPageRelativePath();
    }

    /**
     * Het pad, relatief aan het formulier, van het invoerelement binnen het formulierveld met deze wicket:id.
     */
    protected String veld(String formId, String id) {
        Form<?> form = (Form<?>) tester.getComponentFromLastRenderedPage(pad(formId));
        Component veld = form.visitChildren((Component c, IVisit<Component> visit) -> {
            if (c.getId().equals(id)) {
                visit.stop(c);
            }
        });
        Component invoer = veld instanceof FormComponent<?> || !(veld instanceof MarkupContainer container)
                ? veld
                : container.visitChildren(FormComponent.class, (FormComponent<?> c, IVisit<Component> visit) -> visit.stop(c));
        if (invoer == null) {
            invoer = veld;
        }
        return invoer.getPageRelativePath().substring(form.getPageRelativePath().length() + 1);
    }
}
