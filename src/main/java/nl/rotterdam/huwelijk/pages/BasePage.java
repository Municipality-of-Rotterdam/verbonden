package nl.rotterdam.huwelijk.pages;

import nl.rotterdam.nl_design_system.rotterdam_css.wicket.PatchingNldsRotterdamDesignSystemThemeBehavior;
import org.apache.wicket.markup.html.WebPage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public abstract class BasePage extends WebPage {

    public BasePage() {
        add(PatchingNldsRotterdamDesignSystemThemeBehavior.INSTANCE);
    }

    /** Parses an ISO date string (yyyy-MM-dd); returns {@code null} on blank or invalid input. */
    protected static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
