package nl.rotterdam.verbonden.core.error_common;

import nl.rotterdam.verbonden.core.administration_common.AdministrationAccessDeniedPage;
import nl.rotterdam.verbonden.core.administration_common.AdministrationInternalErrorPage;
import nl.rotterdam.verbonden.core.administration_common.AdministrationPageExpiredPage;
import nl.rotterdam.verbonden.core.burger_common.BurgerAccessDeniedPage;
import nl.rotterdam.verbonden.core.burger_common.BurgerInternalErrorPage;
import nl.rotterdam.verbonden.core.burger_common.BurgerPageExpiredPage;
import org.apache.wicket.DefaultExceptionMapper;
import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;

import java.util.Map;

/**
 * Wicket kent per fouttype één foutpagina (de burgervariant, zie {@code WicketApplication}).
 * Deze mapper vervangt die bij een request onder {@code /beheer} door de beheervariant, zodat een
 * medewerker niet op een burgerpagina (met BSN-logica) terechtkomt.
 */
public class VerbondenExceptionMapper extends DefaultExceptionMapper {

    private static final Map<Class<? extends Page>, Class<? extends Page>> ADMINISTRATION_ERROR_PAGES = Map.of(
            BurgerInternalErrorPage.class, AdministrationInternalErrorPage.class,
            BurgerAccessDeniedPage.class, AdministrationAccessDeniedPage.class,
            BurgerPageExpiredPage.class, AdministrationPageExpiredPage.class
    );

    @Override
    protected RenderPageRequestHandler createPageRequestHandler(PageProvider pageProvider) {
        if (pageProvider.hasPageInstance() || !ServletErrorAttributes.isAdministrationRequest()) {
            return super.createPageRequestHandler(pageProvider);
        }
        Class<? extends Page> administrationErrorPage = ADMINISTRATION_ERROR_PAGES.get(pageProvider.getPageClass());
        return super.createPageRequestHandler(administrationErrorPage != null
                ? new PageProvider(administrationErrorPage)
                : pageProvider);
    }
}
