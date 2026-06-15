package nl.rotterdam.huwelijk.features.marriage_intake.ui;

import nl.rotterdam.huwelijk.features.marriage_intake.application.MarriageIntakeService;
import nl.rotterdam.huwelijk.features.marriage_intake.domain.PasfotoDto;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.Optional;
import java.util.UUID;

/**
 * Wicket shared resource that serves a partner's passport photo from the database.
 * URL parameters: dossierId, bsn.
 */
public class PasfotoResource extends AbstractResource {

    @SpringBean
    private MarriageIntakeService marriageIntakeService;

    public PasfotoResource() {
        Injector.get().inject(this);
    }

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        ResourceResponse response = new ResourceResponse();

        String dossierIdParam = attributes.getParameters().get("dossierId").toString();
        String bsnParam = attributes.getParameters().get("bsn").toString();

        if (dossierIdParam == null || bsnParam == null) {
            response.setStatusCode(404);
            return response;
        }

        UUID dossierId;
        try {
            dossierId = UUID.fromString(dossierIdParam);
        } catch (IllegalArgumentException e) {
            response.setStatusCode(404);
            return response;
        }

        Optional<PasfotoDto> pasfoto = marriageIntakeService.findPasfoto(dossierId, bsnParam);
        if (pasfoto.isEmpty()) {
            response.setStatusCode(404);
            return response;
        }

        PasfotoDto dto = pasfoto.get();
        response.setContentType(dto.contentType());
        response.setContentLength(dto.data().length);
        response.setWriteCallback(new WriteCallback() {
            @Override
            public void writeData(Attributes attributes) {
                attributes.getResponse().write(dto.data());
            }
        });

        return response;
    }
}
