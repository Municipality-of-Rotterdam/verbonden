package nl.rotterdam.huwelijk;

import de.agilecoders.wicket.webjars.WicketWebjars;
import nl.rotterdam.huwelijk.features.baps_administration.ui.BapsToevoegenPage;
import nl.rotterdam.huwelijk.features.baps_administration.ui.BapsWijzigenPage;
import nl.rotterdam.huwelijk.features.baps_administration.ui.BeheerPage;
import nl.rotterdam.huwelijk.features.marriage_intake.ui.HomePage;
import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.FetchMetadataResourceIsolationPolicy;
import org.apache.wicket.protocol.http.ResourceIsolationRequestCycleListener;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class WicketApplication extends WebApplication {

    @Override
    protected IConverterLocator newConverterLocator() {
        ConverterLocator locator = (ConverterLocator) super.newConverterLocator();
        locator.set(LocalDate.class, new IConverter<LocalDate>() {
            @Override
            public LocalDate convertToObject(String value, Locale locale) throws ConversionException {
                if (value == null || value.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (DateTimeParseException e) {
                    throw new ConversionException(e).setResourceKey("IConverter.Date");
                }
            }

            @Override
            public String convertToString(LocalDate value, Locale locale) {
                return value != null ? value.format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
            }
        });
        return locator;
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

    @Override
    public void init() {
        super.init();
        WicketWebjars.install(this);
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));

        getMarkupSettings().setStripWicketTags(true);

        // Enable Wicket's built-in CSRF protection via Fetch Metadata headers.
        // Spring Security's CSRF filter is disabled in SecurityConfig to avoid conflicts
        // with Wicket's own form submission mechanism (see SecurityConfig for details).
        getRequestCycleListeners().add(
                new ResourceIsolationRequestCycleListener(
                        new FetchMetadataResourceIsolationPolicy()));

        mountPage("/beheer", BeheerPage.class);
        mountPage("/beheer/baps/nieuw", BapsToevoegenPage.class);
        mountPage("/beheer/baps/${id}", BapsWijzigenPage.class);
    }
}
