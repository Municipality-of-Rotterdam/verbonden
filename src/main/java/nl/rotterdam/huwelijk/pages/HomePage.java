package nl.rotterdam.huwelijk.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

public class HomePage extends BasePage {

    public HomePage() {
        add(new Label("title", Model.of("Huwelijk POC")));
    }
}
