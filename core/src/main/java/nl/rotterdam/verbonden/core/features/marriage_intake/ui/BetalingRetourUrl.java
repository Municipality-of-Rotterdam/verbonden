package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;

import java.util.UUID;

import static nl.rotterdam.verbonden.core.features.marriage_intake.ui.DossierPageParameterUtil.makeDossierPageParameters;

/**
 * De absolute URL van de {@link BetalingRetourPage}, waar de betaalprovider de burger na het betalen heen stuurt.
 */
final class BetalingRetourUrl {

    private BetalingRetourUrl() {
    }

    static String voor(UUID dossierId) {
        RequestCycle cycle = RequestCycle.get();
        return cycle.getUrlRenderer().renderFullUrl(
                Url.parse(cycle.urlFor(BetalingRetourPage.class, makeDossierPageParameters(dossierId))));
    }
}
