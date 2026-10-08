package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.administration_common.AdministrationBasePage;
import nl.rotterdam.verbonden.core.features.dossier_administration.application.DossierAdministrationService;
import nl.rotterdam.verbonden.core.features.dossier_administration.domain.DossierDetailDto;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.UUID;

/**
 * Basis voor de beheerpagina's van één dossier ({@code /beheer/dossiers/{dossierId}/...}). Laadt het dossier;
 * bij een onbekend of ongeldig dossier-id gaat de medewerker terug naar het overzicht.
 */
abstract class DossierBeheerBasePage extends AdministrationBasePage {

    static final String DOSSIER_ID = "dossierId";

    @SpringBean
    protected DossierAdministrationService dossierAdministrationService;

    protected final UUID dossierId;

    /** Het dossier zoals het was bij het openen van de pagina. */
    protected final DossierDetailDto dossier;

    DossierBeheerBasePage(PageParameters params) {
        super();
        dossierId = parseDossierId(params);
        try {
            dossier = dossierAdministrationService.findDetail(dossierId);
        } catch (IllegalArgumentException e) {
            throw new RestartResponseException(DossierAdministrationPage.class);
        }
    }

    static PageParameters parametersVoor(UUID dossierId) {
        return new PageParameters().set(DOSSIER_ID, dossierId.toString());
    }

    private static UUID parseDossierId(PageParameters params) {
        try {
            return UUID.fromString(params.get(DOSSIER_ID).toString(""));
        } catch (IllegalArgumentException e) {
            throw new RestartResponseException(DossierAdministrationPage.class);
        }
    }

    /**
     * Terug naar de detailpagina, die het dossier opnieuw laadt.
     */
    protected void naarDetailPagina() {
        setResponsePage(DossierDetailPage.class, parametersVoor(dossierId));
    }
}
