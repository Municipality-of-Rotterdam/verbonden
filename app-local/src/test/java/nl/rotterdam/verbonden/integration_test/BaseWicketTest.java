package nl.rotterdam.verbonden.integration_test;


import nl.rotterdam.verbonden.WicketApplication;
import org.apache.wicket.util.tester.WicketTester;
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
}
