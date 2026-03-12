package nl.rotterdam.huwelijk.features.baps_administration;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import nl.rotterdam.nl_design_system.rotterdam_css.wicket.PatchingNldsRotterdamDesignSystemThemeBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public abstract class BeheerBasePage extends WebPage {

    private static final CssReferenceHeaderItem BOOTSTRAP_UTILITIES_HEADER_ITEM =
            CssHeaderItem.forReference(new WebjarsCssResourceReference("bootstrap/current/css/bootstrap-utilities.min.css"));

    public BeheerBasePage() {
        add(PatchingNldsRotterdamDesignSystemThemeBehavior.INSTANCE);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(BOOTSTRAP_UTILITIES_HEADER_ITEM);
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
