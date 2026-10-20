package nl.rotterdam.verbonden.core.features.dossier_administration.ui;

import nl.rotterdam.verbonden.core.domain.ValueHolder;
import nl.rotterdam.verbonden.core.features.dossier_administration.application.DossierAdministrationService;
import nl.rotterdam.verbonden.core.identity.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeResource;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Alle gegevens van één dossier als JSON, gemount op {@code /beheer/dossiers/${dossierId}/json} en daarmee
 * net als de beheerpagina's alleen bereikbaar voor medewerkers (zie {@link AuthorizeResource} hieronder). Elke
 * opvraging komt in het inzagelog.
 */
public class DossierJsonResourceReference extends ResourceReference {

    public DossierJsonResourceReference() {
        super(DossierJsonResourceReference.class, "dossier-json");
    }

    @Override
    public IResource getResource() {
        return new DossierJsonResource();
    }

    /**
     * Een resource valt niet onder de {@code @AuthorizeInstantiation} van de beheerpagina's; dezelfde rollen dus
     * hier.
     */
    @AuthorizeResource({Roles.BEHEER, Roles.SUPERUSER, Roles.KEYUSER, Roles.WERKVERDELER, Roles.PLANNER,
            Roles.MEDEWERKER})
    private static class DossierJsonResource extends AbstractResource {

        /** Value types (BSN, e-mailadres, ...) als hun waarde, niet als {@code {"value": ...}}. */
        private static final JsonMapper JSON = JsonMapper.builder()
                .addModule(new SimpleModule().addSerializer(ValueHolder.class, new ValueSerializer<>() {
                    @Override
                    public void serialize(ValueHolder value, JsonGenerator gen, SerializationContext ctxt) {
                        ctxt.writeValue(gen, value.getValue());
                    }
                }))
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();

        @SpringBean
        private DossierAdministrationService dossierAdministrationService;

        DossierJsonResource() {
            Injector.get().inject(this);
        }

        @Override
        protected ResourceResponse newResourceResponse(Attributes attributes) {
            ResourceResponse response = new ResourceResponse();
            UUID dossierId;
            try {
                dossierId = UUID.fromString(attributes.getParameters().get(DossierBeheerBasePage.DOSSIER_ID).toString(""));
            } catch (IllegalArgumentException e) {
                return response.setError(404);
            }

            byte[] json;
            try {
                json = JSON.writeValueAsBytes(dossierAdministrationService.findDetail(dossierId));
            } catch (IllegalArgumentException e) {
                // Onbekend dossier
                return response.setError(404);
            }

            response.setContentType("application/json");
            response.setTextEncoding(StandardCharsets.UTF_8.name());
            response.setFileName("dossier-" + dossierId + ".json");
            response.setContentDisposition(ContentDisposition.INLINE);
            response.setContentLength(json.length);
            response.disableCaching();
            response.setWriteCallback(new WriteCallback() {
                @Override
                public void writeData(Attributes attributes) {
                    attributes.getResponse().write(json);
                }
            });
            return response;
        }
    }
}
