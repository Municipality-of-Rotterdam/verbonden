package nl.rotterdam.verbonden.core.features.marriage_intake.ui;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class DossierPageParameterUtil {

    public static final String PARAM_DOSSIER_ID = "dossierId";
    public static final String PARAM_EXTRAS_DOORLOPEN = "extrasDoorlopen";

    @Nullable
    public static UUID extractDossierId(PageParameters parameters) {
        String dossierIdStr = parameters.get(PARAM_DOSSIER_ID).toOptionalString();
        if (dossierIdStr != null && !dossierIdStr.isBlank()) {
            return UUID.fromString(dossierIdStr);
        }

        return null;
    }

    public static PageParameters makeDossierPageParameters(@Nullable UUID dossierId) {
        return new PageParameters().add(PARAM_DOSSIER_ID, dossierId);
    }

    public static PageParameters makeDossierPageParameters(@Nullable UUID dossierId, boolean extrasDoorlopen) {
        PageParameters parameters = makeDossierPageParameters(dossierId);
        if (extrasDoorlopen) {
            parameters.add(PARAM_EXTRAS_DOORLOPEN, true);
        }
        return parameters;
    }
}
